/*
	Name: Naman Gangwani
	NetID: nkg160030
	Date: February 4, 2017
	Description: Reserves seats or best available seats based on user input
 */

import java.util.*;
import java.io.*;

public class TicketReservationSystem {
	
	public static void main(String[] args) throws IOException
	{
		Scanner input = new Scanner(System.in);
		int chosen = 0; // User choice for auditorium
		int[] ticketSales = new int[3]; // Keeps track of ticket sales for the three auditoriums
		
		do
		{
			// Prints out options
			System.out.println("1. Auditorium 1\n2. Auditorium 2\n3. Auditorium 3\n4. Exit\n");
			chosen = input.nextInt(); 
			
			if (chosen == 1 || chosen == 2 || chosen == 3)
			{
				char[][] seats = readAuditorium("A"+chosen+".txt"); // Gets the reserved and open seat information from the file
				displayAuditorium(seats);
				int chosenRow; // Keeps track of the user's choice of row
				do
				{
					System.out.print("\nEnter a row number: ");
					chosenRow = input.nextInt();
					// Checks to see if it's outside the bounds of the auditorium
					if (chosenRow < 1 || chosenRow > seats[0].length)
						System.out.println("\n** Please only choose a valid row number.");
				} while (chosenRow < 1 || chosenRow > seats[0].length); // Prompts user to enter a row number until it is valid
				
				int chosenColumn;// Keeps track of the user's starting seat
				boolean validSeat = false;
				do
				{
					System.out.print("Enter a starting seat number: ");
					chosenColumn = input.nextInt();
					
					// Checks to see if it's outside the bounds of the auditorium
					if (chosenColumn < 1 || chosenColumn > seats.length) 
						System.out.println("\n** Please only choose a valid starting seat number.\n");
					// Checks whether the chosen seat is already reserved
					/*
					else if (checkIfAlreadyReserved(seats, chosenRow - 1, chosenColumn - 1))
						System.out.println("\n** That seat is already reserved. Please choose another.\n");*/
					else
						validSeat = true;
				} while (!validSeat); // Prompts user to enter a starting seat number until it is valid
				
				int numTickets; // Keeps track of the number of sequential seats the user wants to reserve
				boolean validNumberOfTickets = false;
				do
				{
					System.out.print("Enter the number of tickets: ");
					numTickets = input.nextInt();
					// Checks to see if the user is requesting a valid amount of seats based the open seats in the row
					if (areEnoughSeatsInRow(seats, chosenRow - 1, numTickets))
						validNumberOfTickets = true;
					else
						System.out.println("\n** Please only enter a valid number of tickets for row "+chosenRow+".\n");
				} while (!validNumberOfTickets); // Prompts user to enter a number of tickets until it is valid
				
				// If seats for the given number of tickets can be reserved sequentially to the right of the starting seat 
				if (checkAvailability(seats, chosenRow - 1, chosenColumn - 1, numTickets))
				{
					// Changes the status of those seats to reserved
					for (int i = chosenColumn - 1; i < chosenColumn - 1 + numTickets; i++)
						seats[i][chosenRow - 1] = '.';
					updateAuditoriumFile("A"+chosen+".txt", seats); // Changes the file to update the reserved seats
					ticketSales[chosen - 1]+=numTickets; // Adds to the ticket count of the corresponding auditorium
					System.out.println("\nSeat(s) successfully reserved!");
				} else
				{
					int centerSeat = (int) Math.ceil((double) seats.length/2) - 1; // Finds the center seat
					int middlePerson = (int) Math.ceil((double) numTickets/2) - 1; // Finds the middle seat in the set of people
					int distanceFromCenter = 100000000;
					int startingSeat = 0;
					boolean foundBestAvailable = false;
					
					// Checks the entire row, seat by seat, to see if sequential seats can be found for best available ones
					for (int i = 0; i < seats.length; i++)
						if (checkAvailability(seats, chosenRow - 1, i, numTickets)) // If sequential seats can be found starting from that seat
						{
							foundBestAvailable = true;
							//System.out.println(i + " " +Math.abs(centerSeat - (i + middlePerson)));
							if (Math.abs(centerSeat - (i + middlePerson)) < distanceFromCenter) // If the middle person in the set is as close to the center as possible
							{
								distanceFromCenter = Math.abs(centerSeat - (i + middlePerson)); // Sets a new minimum distance from the center
								startingSeat = i; // Sets the new best starting seat
							}
						}
					
					if (foundBestAvailable) // If there was a best available match found
					{
						// Prompts user to accept or decline the best available seats
						System.out.print("\nBest available seats on row "+chosenRow+": ");
						for (int i = startingSeat; i < startingSeat + numTickets; i++)
							System.out.print((i+1) + " ");
						System.out.print("\nWould you like to buy these seats? (Y/N): ");
						String buying = input.next();
						if (buying.toLowerCase().equals("y")) // If the user wants to buy the best available seats
						{
							// Changes the status of those seats to reserved
							for (int i = startingSeat; i < startingSeat + numTickets; i++)
								seats[i][chosenRow - 1] = '.';
							updateAuditoriumFile("A"+chosen+".txt", seats); // Changes the file to update the reserved seats
							ticketSales[chosen - 1]+=numTickets; // Adds to the ticket count of the corresponding auditorium
							System.out.println("\nBest available seats successfully reserved!");
						} else
							System.out.println("\nBest available seats not reserved.");
					} else
						System.out.println("\nCould not find best available seats.");
				}
				System.out.println("Returning to menu...\n");
			} else
				if (chosen != 4)
					System.out.println("** Please only select options 1-4.\n");
		} while (chosen != 4); // Continues offering options until the user chooses to exit
		
		// Prints a report of the final seat reservations, open seats, and ticket sales
		System.out.println("\nAuditorium    Seats Reserved    Open Seats    Ticket Sales");
		int[] total = new int[3]; // Keeps track of the total number of reservations, open seats, and sales
		for (int i = 1; i <=3; i++)
		{
			int[] report = countSeats("A"+i+".txt"); // Gets report for the specific auditorium
			// Adds report information to the total count
			total[0]+=report[0];
			total[1]+=report[1];
			total[2]+=ticketSales[i -1];
			// Prints seat reservations, open seats, and ticket sales for the individual auditorium
			System.out.println("    "+i+"\t\t    "+report[0]+"\t\t    "+report[1]+"\t\t    $"+report[0]*7);
		}
		
		// Prints the total number of seats reserved, open seats, and ticket sales
		System.out.println("\n  TOTAL \t    "+total[0]+"\t\t    "+total[1]+"\t\t    $"+total[0]*7);
		
		input.close();
	}
	
	// Reads the content of the given file and stores it into a 2D array of characters to return
	public static char[][] readAuditorium(String fileName) throws FileNotFoundException
	{
		Scanner auditorium = new Scanner(new File(fileName));
		ArrayList<String> lines = new ArrayList<String>();
		int numRows = 0; // Total number of rows in the file
		while (auditorium.hasNextLine())
		{
			String line = auditorium.nextLine();
			if (!line.equals(""))
			{
				lines.add(line); // Adds to ArrayList for later reading to add to the 2D array
				numRows+=1;
			}
		}
		
		char[][] seats = new char[lines.get(0).length()][numRows]; // Char array based on the amount of rows and columns in the file
		for (int i = 0; i < numRows; i++) // Loops through rows
			for (int j = 0; j < lines.get(i).length(); j++) // Loops through columns for each row
				seats[j][i] = lines.get(i).charAt(j); // Adds to 2D array character by character
		
		auditorium.close();
		return seats;
	}
	
	// Updates the given file to the content of the given 2D array of characters
	public static void updateAuditoriumFile(String fileName, char[][] seats) throws FileNotFoundException
	{
		PrintWriter auditorium = new PrintWriter(fileName);
		auditorium.print(""); // Clears the file
		// Loops through the 2D array to write to the file character by character
		for (int i = 0; i < seats[0].length; i++)
		{
			for (int j = 0; j < seats.length; j++)
				auditorium.print(seats[j][i]);
			auditorium.println();
		}
		auditorium.close();
	}
	
	// Displays the given 2D array, which contains the content of the auditorium (reserved and open seats) 
	public static void displayAuditorium(char[][] seats)
	{
		System.out.print("\n  ");
		int columnNumber = 0;
		// Prints the column/seat numbers at the top
		for (int j = 0; j < seats.length; j++)
		{
			columnNumber+=1;
			if (columnNumber == 10)
				columnNumber = 0; // Resets back to 0 when it reaches 10 to prevent double digits in labeling
			System.out.print(columnNumber);
		}
		
		int rowNumber = 0;
		// Prints the row number alongside all the reserved and open seats in that row
		for (int i = 0; i < seats[0].length; i++)
		{
			System.out.print("\n" +(rowNumber+=1)+ " "); // Prints row number
			for (int j = 0; j < seats.length; j++)
				System.out.print(seats[j][i]); // Prints seats character by character
		}
		System.out.println("");
	}
	
	// Checks if the given seat from its row and column numbers is reserved in a specific auditorium or not
	public static boolean checkIfAlreadyReserved(char[][] seats, int rowNumber, int columnNumber)
	{
		if (seats[columnNumber][rowNumber] == '.') // If, at that position in the 2D array, it is marked by a '.'
			return true; // Its corresponding seat is reserved
		return false;
	}
	
	// Checks if the amount of seats in a given row in a given auditorium is sufficient enough for the amount of tickets requested
	public static boolean areEnoughSeatsInRow(char[][] seats, int rowNumber, int numTickets)
	{
		int numSeatsAvailable = 0;
		for (int i = 0; i < seats.length; i++)
			if (seats[i][rowNumber] == '#')
				numSeatsAvailable+=1; // Adds one to the available seats if there is a seat marked by '#'
		if (numTickets > 0 && numTickets <= numSeatsAvailable)
			return true; // There are enough seats if the amount of seats available can cover the number of tickets
		return false;
	}
	
	/*
	 	Checks if a given seat from its row and column numbers in a given auditorium has enough 
	 	sequential seats from left to right for the given quantity of seats
	 */
	public static boolean checkAvailability(char[][] seats, int rowNumber, int columnNumber, int quantity)
	{
		if (quantity == 1 && seats[columnNumber][rowNumber] == '#') // If there's only one person for an open seat
			return true; // It is available
		else if (seats[columnNumber][rowNumber] == '.') // If the given seat is already reserved
			return false; // It is not available
		else
		{
			int count = 0; // Keeps count of how many sequential seats there are from the starting seat
			for (int i = 1; i <= quantity; i++)
				if (columnNumber < seats.length) // If the current seat number is in the bounds of the auditorium
					if (seats[columnNumber][rowNumber] == '#')
					{
						count+=1;
						columnNumber+=1;
					}
					else
						break;
				else
					break;
			if (count == quantity) // If the count of sequential seats matches up with the amount we wanted
				return true; // There are an available amount of seats
		}
		return false;
	}
	
	// Counts and returns the total number of reserved and open seats in a specified file
	public static int[] countSeats(String fileName) throws FileNotFoundException
	{
		int[] seats = new int[2]; // Keeps track of reserved and open seats, respectively
		Scanner auditorium = new Scanner(new File(fileName));
		while (auditorium.hasNextLine()) // Loops through all the content of the file
		{
			String line = auditorium.nextLine();
			if (!line.equals("")) // Assures that it's not reading an empty line
				for (int i = 0; i < line.length(); i++)
					if (line.charAt(i) == '.')
						seats[0]+=1; // Adds to reserved seat count if a character is marked by a '.'
					else if (line.charAt(i) == '#')
						seats[1]+=1; // Adds to open seat count if a character is marked by a '#'
		}
		auditorium.close();
		return seats;
	}
}
