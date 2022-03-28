package solution;

import java.nio.file.Paths;
import java.time.LocalDate;


import baseclasses.DataLoadingException;
import baseclasses.IAircraftDAO;
import baseclasses.ICrewDAO;
import baseclasses.IPassengerNumbersDAO;
import baseclasses.IRouteDAO;

/**
 * This class allows you to run the code in your classes yourself, for testing and development
 */
public class Main {

	public static void main(String[] args) {	
		IAircraftDAO aircraft = new AircraftDAO();
		ICrewDAO crew = new CrewDAO();
		IRouteDAO route =new RouteDAO();
		IPassengerNumbersDAO passnum = new PassengerNumbersDAO();
		Scheduler sch = new Scheduler();

		try {
			aircraft.loadAircraftData(Paths.get("./data/aircraft.csv"));
//			aircraft.loadAircraftData(Paths.get("./data/mini_aircraft.csv"));
			//aircraft.loadAircraftData(Paths.get("./data/schedule_aircraft.csv"));
		}
		catch (DataLoadingException dle) {
			System.err.println("Error loading aircraft data");
			dle.printStackTrace();
		}

		//		for(int i=0; i<aircraft.findAircraftBySeats(180).size();i++)
		//			System.out.println(aircraft.findAircraftBySeats(180).get(i).getSeats());

		try {
			crew.loadCrewData(Paths.get("./data/crew.json"));
			//crew.loadCrewData(Paths.get("./data/mini_crew.json"));
			//crew.loadCrewData(Paths.get("./data/schedule_crew.json"));
			//crew.loadCrewData(Paths.get("./data/malformed_crew1.json"));
		}
		catch (DataLoadingException dle) {
			System.err.println("Error loading crew data");
			dle.printStackTrace();
		}

		//		System.out.println(crew.getNumberOfPilots());

		try {
			route.loadRouteData(Paths.get("./data/routes.xml"));
			//route.loadRouteData(Paths.get("./data/mini_routes.xml"));
			//route.loadRouteData(Paths.get("./data/schedule_routes.xml"));
			//route.loadRouteData(Paths.get("./data/malformed_routes1.xml"));

		}
		catch (DataLoadingException dle) {
			System.err.println("Error loading crew data");
			dle.printStackTrace();
		}

		//		System.out.println(route.findRoutesDepartingAirport("MAN").size());

		try {
			passnum.loadPassengerNumbersData(Paths.get("./data/passengernumbers.db"));
//			passnum.loadPassengerNumbersData(Paths.get("./data/mini_passengers.db"));
			//passnum.loadPassengerNumbersData(Paths.get("./data/schedule_passengers.db"));
		}
		catch (DataLoadingException dle) {
			System.err.println("Error loading crew data");
			dle.printStackTrace();
		}
		System.out.println("Load Succecess");
		System.out.println("");
		
//		LocalTime TimeWorked = LocalTime.parse("00:10");
//		System.out.println(TimeWorked);
		
//		System.out.println(passnum.getNumberOfEntries());

		sch.generateSchedule(aircraft, crew, route, passnum, LocalDate.parse("2021-07-01"), LocalDate.parse("2021-08-31"));

	}

}
