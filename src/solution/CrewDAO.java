package solution;
import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import baseclasses.CabinCrew;
import baseclasses.Crew;
import baseclasses.DataLoadingException;
import baseclasses.ICrewDAO;
import baseclasses.Pilot;
import baseclasses.Pilot.Rank;

/**
 * The CrewDAO is responsible for loading data from JSON-based crew files 
 * It contains various methods to help the scheduler find the right pilots and cabin crew
 */
public class CrewDAO implements ICrewDAO {

	List<Pilot> pilot = new ArrayList<>();
	List<CabinCrew> cabincrew = new ArrayList<>();

	/**
	 * Loads the crew data from the specified file, adding them to the currently loaded crew
	 * Multiple calls to this function, perhaps on different files, would thus be cumulative
	 * @param p A Path pointing to the file from which data could be loaded
	 * @throws DataLoadingException if anything goes wrong. The exception's "cause" indicates the underlying exception
	 */
	@Override
	public void loadCrewData(Path p) throws DataLoadingException {
		try(BufferedReader br = Files.newBufferedReader(p))
		{

			String json ="["; String line ="";
			while((line=br.readLine())!=null) {json = json +line;}
			json = json +"]";

			JSONArray root = new JSONArray(json);

			for(int i = 0; i<root.getJSONObject(0).getJSONArray("pilots").length();i++) 
			{
				String forename = root.getJSONObject(0).getJSONArray("pilots").getJSONObject(i).getString("forename");
				String surname = root.getJSONObject(0).getJSONArray("pilots").getJSONObject(i).getString("surname");
				String homeairport = root.getJSONObject(0).getJSONArray("pilots").getJSONObject(i).getString("home_airport");
				String rank = root.getJSONObject(0).getJSONArray("pilots").getJSONObject(i).getString("rank");

				Pilot a = new Pilot();
				a.setForename(forename);
				a.setSurname(surname);
				a.setHomeBase(homeairport);
				a.setRank(Rank.valueOf(rank));
				for(int j =0; j<root.getJSONObject(0).getJSONArray("pilots").getJSONObject(i).getJSONArray("type_ratings").length();j++)
				{
					String typerating = root.getJSONObject(0).getJSONArray("pilots").getJSONObject(i).getJSONArray("type_ratings").getString(j);
					a.setQualifiedFor(typerating);
				}

				pilot.add(a);
			}

			for(int i = 0; i<root.getJSONObject(0).getJSONArray("cabincrew").length();i++) 
			{
				String forename = root.getJSONObject(0).getJSONArray("cabincrew").getJSONObject(i).getString("forename");
				String surname = root.getJSONObject(0).getJSONArray("cabincrew").getJSONObject(i).getString("surname");
				String homeairport = root.getJSONObject(0).getJSONArray("cabincrew").getJSONObject(i).getString("home_airport");

				CabinCrew a = new CabinCrew();
				a.setForename(forename);
				a.setSurname(surname);
				a.setHomeBase(homeairport);
				for(int j =0; j<root.getJSONObject(0).getJSONArray("cabincrew").getJSONObject(i).getJSONArray("type_ratings").length();j++){
					String typerating = root.getJSONObject(0).getJSONArray("cabincrew").getJSONObject(i).getJSONArray("type_ratings").getString(j);
					a.setQualifiedFor(typerating);}

				cabincrew.add(a);
			}

		}
		catch (Throwable e) {
			//There was a problem reading the file
			throw new DataLoadingException(e);
		}

	}

	/**
	 * Returns a list of all the cabin crew based at the airport with the specified airport code
	 * @param airportCode the three-letter airport code of the airport to check for
	 * @return a list of all the cabin crew based at the airport with the specified airport code
	 */
	@Override
	public List<CabinCrew> findCabinCrewByHomeBase(String airportCode) {

		List<CabinCrew> Crew = new ArrayList<>();

		for(int i =0; i<cabincrew.size();i++) 
			if(airportCode.equals(cabincrew.get(i).getHomeBase()))
				Crew.add(cabincrew.get(i));

		return Crew;
	}

	/**
	 * Returns a list of all the cabin crew based at a specific airport AND qualified to fly a specific aircraft type
	 * @param typeCode the type of plane to find cabin crew for
	 * @param airportCode the three-letter airport code of the airport to check for
	 * @return a list of all the cabin crew based at a specific airport AND qualified to fly a specific aircraft type
	 */
	@Override
	public List<CabinCrew> findCabinCrewByHomeBaseAndTypeRating(String typeCode, String airportCode) {

		List<CabinCrew> Crew = new ArrayList<>();

		for(int i =0; i<cabincrew.size();i++) 
			for(int j =0; j<cabincrew.get(i).getTypeRatings().size();j++)
				if(airportCode.equals(cabincrew.get(i).getHomeBase()) && typeCode.equals(cabincrew.get(i).getTypeRatings().get(j)))
					Crew.add(cabincrew.get(i));

		return Crew;
	}

	/**
	 * Returns a list of all the cabin crew currently loaded who are qualified to fly the specified type of plane
	 * @param typeCode the type of plane to find cabin crew for
	 * @return a list of all the cabin crew currently loaded who are qualified to fly the specified type of plane
	 */
	@Override
	public List<CabinCrew> findCabinCrewByTypeRating(String typeCode) {

		List<CabinCrew> Crew = new ArrayList<>();

		for(int i =0; i<cabincrew.size();i++) 
			for(int j =0; j<cabincrew.get(i).getTypeRatings().size();j++)
				if(typeCode.equals(cabincrew.get(i).getTypeRatings().get(j)))
					Crew.add(cabincrew.get(i));

		return Crew;
	}

	/**
	 * Returns a list of all the pilots based at the airport with the specified airport code
	 * @param airportCode the three-letter airport code of the airport to check for
	 * @return a list of all the pilots based at the airport with the specified airport code
	 */
	@Override
	public List<Pilot> findPilotsByHomeBase(String airportCode) {
		List<Pilot> Pilot = new ArrayList<>();

		for(int i =0; i<pilot.size();i++) 
			if(airportCode.equals(pilot.get(i).getHomeBase()))
				Pilot.add(pilot.get(i));

		return Pilot;
	}

	/**
	 * Returns a list of all the pilots based at a specific airport AND qualified to fly a specific aircraft type
	 * @param typeCode the type of plane to find pilots for
	 * @param airportCode the three-letter airport code of the airport to check for
	 * @return a list of all the pilots based at a specific airport AND qualified to fly a specific aircraft type
	 */
	@Override
	public List<Pilot> findPilotsByHomeBaseAndTypeRating(String typeCode, String airportCode) {

		List<Pilot> Pilot = new ArrayList<>();

		for(int i =0; i<pilot.size();i++) 
			for(int j =0; j<pilot.get(i).getTypeRatings().size();j++)
				if(airportCode.equals(pilot.get(i).getHomeBase()) && typeCode.equals(pilot.get(i).getTypeRatings().get(j)))
					Pilot.add(pilot.get(i));

		return Pilot;
	}

	/**
	 * Returns a list of all the pilots currently loaded who are qualified to fly the specified type of plane
	 * @param typeCode the type of plane to find pilots for
	 * @return a list of all the pilots currently loaded who are qualified to fly the specified type of plane
	 */
	@Override
	public List<Pilot> findPilotsByTypeRating(String typeCode) {

		List<Pilot> Pilot = new ArrayList<>();

		for(int i =0; i<pilot.size();i++) 
			for(int j =0; j<pilot.get(i).getTypeRatings().size();j++)
				if(typeCode.equals(pilot.get(i).getTypeRatings().get(j)))
					Pilot.add(pilot.get(i));

		return Pilot;
	}

	/**
	 * Returns a list of all the cabin crew currently loaded
	 * @return a list of all the cabin crew currently loaded
	 */
	@Override
	public List<CabinCrew> getAllCabinCrew() {
		ArrayList<CabinCrew> AllCabinCrew = new ArrayList<CabinCrew>(cabincrew);
		return AllCabinCrew;
	}

	/**
	 * Returns a list of all the crew, regardless of type
	 * @return a list of all the crew, regardless of type
	 */
	@Override
	public List<Crew> getAllCrew() {
		ArrayList<Crew> AllCrew = new ArrayList<Crew>();
		AllCrew.addAll(pilot);
		AllCrew.addAll(cabincrew);
		return AllCrew;
	}

	/**
	 * Returns a list of all the pilots currently loaded
	 * @return a list of all the pilots currently loaded
	 */
	@Override
	public List<Pilot> getAllPilots() {
		ArrayList<Pilot> AllPilots = new ArrayList<Pilot>(pilot);
		return AllPilots;
	}

	@Override
	public int getNumberOfCabinCrew() {
		return cabincrew.size();
	}

	/**
	 * Returns the number of pilots currently loaded
	 * @return the number of pilots currently loaded
	 */
	@Override
	public int getNumberOfPilots() {
		return pilot.size();
	}

	/**
	 * Unloads all of the crew currently loaded, ready to start again if needed
	 */
	@Override
	public void reset() {
		pilot.clear();
		cabincrew.clear();

	}

}
