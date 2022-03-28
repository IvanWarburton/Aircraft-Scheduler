package solution;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.HashMap;

import baseclasses.DataLoadingException;
import baseclasses.IPassengerNumbersDAO;

/**
 * The PassengerNumbersDAO is responsible for loading an SQLite database
 * containing forecasts of passenger numbers for flights on dates
 */
public class PassengerNumbersDAO implements IPassengerNumbersDAO {

	HashMap<String, Integer> passengerNumbers = new HashMap<>();

	/**
	 * Returns the number of passenger number entries in the cache
	 * @return the number of passenger number entries in the cache
	 */
	@Override
	public int getNumberOfEntries() {
		return passengerNumbers.size();
	}

	/**
	 * Returns the predicted number of passengers for a given flight on a given date, or -1 if no data available
	 * @param flightNumber The flight number of the flight to check for
	 * @param date the date of the flight to check for
	 * @return the predicted number of passengers, or -1 if no data available
	 */
	@Override
	public int getPassengerNumbersFor(int flightNumber, LocalDate date) {

		String key = date.toString() + Integer.toString(flightNumber);

		if(passengerNumbers.containsKey(key)) 
			return passengerNumbers.get(key);
		else 
			return -1;
	}

	/**
	 * Loads the passenger numbers data from the specified SQLite database into a cache for future calls to getPassengerNumbersFor()
	 * Multiple calls to this method are additive, but flight numbers/dates previously cached will be overwritten
	 * The cache can be reset by calling reset() 
	 * @param p The path of the SQLite database to load data from
	 * @throws DataLoadingException If there is a problem loading from the database
	 */
	@Override
	public void loadPassengerNumbersData(Path p) throws DataLoadingException {

		Connection c = null;

		try 
		{
			c = DriverManager.getConnection("jdbc:sqlite:" + p);	
			Statement s1 =c.createStatement();
			ResultSet rs1 = s1.executeQuery("select * from PassengerNumbers;");

			while(rs1.next()) 
			{				
				String Date = rs1.getString("Date");
				String FlightNumber = rs1.getString("FlightNumber");
				int LoadEstimate = rs1.getInt("LoadEstimate");
				
				passengerNumbers.put(Date + FlightNumber, LoadEstimate);
			}
		}
		catch(Throwable se)
		{
			throw new DataLoadingException(se);
		}

	}

	/**
	 * Removes all data from the DAO, ready to start again if needed
	 */
	@Override
	public void reset() {
		passengerNumbers.clear();
	}

}
