package solution;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.*;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.*;
import baseclasses.DataLoadingException;
import baseclasses.IRouteDAO;
import baseclasses.Route;

/**
 * The RouteDAO parses XML files of route information, each route specifying
 * where the airline flies from, to, and on which day of the week
 */
public class RouteDAO implements IRouteDAO {

	List<Route> route = new ArrayList<>();

	/**
	 * Finds all flights that depart on the specified day of the week
	 * @param dayOfWeek A three letter day of the week, e.g. "Tue"
	 * @return A list of all routes that depart on this day
	 */
	@Override
	public List<Route> findRoutesByDayOfWeek(String dayOfWeek) {
		List<Route> DOWRoute = new ArrayList<>();

		for(int i =0; i<route.size();i++) 
			if(dayOfWeek.equals(route.get(i).getDayOfWeek()))
				DOWRoute.add(route.get(i));

		return DOWRoute;
	}

	/**
	 * Finds all of the flights that depart from a specific airport on a specific day of the week
	 * @param airportCode the three letter code of the airport to search for, e.g. "MAN"
	 * @param dayOfWeek the three letter day of the week code to searh for, e.g. "Tue"
	 * @return A list of all routes from that airport on that day
	 */
	@Override
	public List<Route> findRoutesByDepartureAirportAndDay(String airportCode, String dayOfWeek) {
		List<Route> DAnDRoute = new ArrayList<>();

		for(int i =0; i<route.size();i++)
			if(dayOfWeek.equals(route.get(i).getDayOfWeek()) && airportCode.equals(route.get(i).getDepartureAirportCode()))
				DAnDRoute.add(route.get(i));

		return DAnDRoute;
	}

	/**
	 * Finds all of the flights that depart from a specific airport
	 * @param airportCode the three letter code of the airport to search for, e.g. "MAN"
	 * @return A list of all of the routes departing the specified airport
	 */
	@Override
	public List<Route> findRoutesDepartingAirport(String airportCode) {
		List<Route> DARoute = new ArrayList<>();

		for(int i =0; i<route.size();i++) 
			if(airportCode.equals(route.get(i).getDepartureAirportCode()))
				DARoute.add(route.get(i));

		return DARoute;
	}

	/**
	 * Finds all of the flights that depart on the specified date
	 * @param date the date to search for
	 * @return A list of all routes that dpeart on this date
	 */
	@Override
	public List<Route> findRoutesbyDate(LocalDate date) {

		String dayOfWeekFull = date.getDayOfWeek().toString().toLowerCase().substring(0, 3);
		String DayOfWeekShort = dayOfWeekFull.substring(0,1).toUpperCase() + dayOfWeekFull.substring(1);

		List<Route> RouteBD = new ArrayList<>();

		for(int i =0; i<route.size();i++) 
			if(DayOfWeekShort.equals(route.get(i).getDayOfWeek()))
				RouteBD.add(route.get(i));

		return RouteBD;
	}

	/**
	 * Returns The full list of all currently loaded routes
	 * @return The full list of all currently loaded routes
	 */
	@Override
	public List<Route> getAllRoutes() {
		return route;
	}

	/**
	 * Returns The number of routes currently loaded
	 * @return The number of routes currently loaded
	 */
	@Override
	public int getNumberOfRoutes() {
		return route.size();
	}

	/**
	 * Loads the route data from the specified file, adding them to the currently loaded routes
	 * Multiple calls to this function, perhaps on different files, would thus be cumulative
	 * @param p A Path pointing to the file from which data could be loaded
	 * @throws DataLoadingException if anything goes wrong. The exception's "cause" indicates the underlying exception
	 */
	@Override
	public void loadRouteData(Path arg0) throws DataLoadingException {
		try {

			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = db.parse(""+arg0);

			Element root = doc.getDocumentElement();

			NodeList children = root.getChildNodes();
			for(int i=0;i<children.getLength();i++) {
				Node c = children.item(i);
				if(c.getNodeName().equals("Route")) {
					NodeList grandchildren = c.getChildNodes();
					Route a = new Route();

					for(int j=0;j<grandchildren.getLength();j++) {
						Node d = grandchildren.item(j);

						if(d.getNodeName().equals("FlightNumber")) {
							int flightnumber = Integer.parseInt(d.getChildNodes().item(0).getNodeValue());
							a.setFlightNumber(flightnumber);
						}

						if(d.getNodeName().equals("DayOfWeek")) {
							String dayofweek = d.getChildNodes().item(0).getNodeValue();
							a.setDayOfWeek(dayofweek);
						}

						
						if(d.getNodeName().equals("DepartureTime")) {
							LocalTime departuretime = LocalTime.parse(d.getChildNodes().item(0).getNodeValue());
							a.setDepartureTime(departuretime);
						}

						if(d.getNodeName().equals("DepartureAirport")) {
							String departureairport = d.getChildNodes().item(0).getNodeValue();
							a.setDepartureAirport(departureairport);
						}

						if(d.getNodeName().equals("DepartureAirportIATACode")) {
							String departureairportcode = d.getChildNodes().item(0).getNodeValue();
							a.setDepartureAirportCode(departureairportcode);
						}

						if(d.getNodeName().equals("ArrivalTime")) {
							LocalTime arrivaltime = LocalTime.parse(d.getChildNodes().item(0).getNodeValue());
							a.setArrivalTime(arrivaltime);
						}

						if(d.getNodeName().equals("ArrivalAirport")) {
							String arrivalairport = d.getChildNodes().item(0).getNodeValue();
							a.setArrivalAirport(arrivalairport);
						}

						if(d.getNodeName().equals("ArrivalAirportIATACode")) {
							String arrivalairportcode = d.getChildNodes().item(0).getNodeValue();
							a.setArrivalAirportCode(arrivalairportcode);
						}

						if(d.getNodeName().equals("Duration")) {
							Duration duration = Duration.parse(d.getChildNodes().item(0).getNodeValue());
							a.setDuration(duration);
						}
					}
					route.add(a);
				}
			}
		}
		catch (Throwable e) {
			//There was a problem reading the file
			throw new DataLoadingException(e);
		}

	}

	/**
	 * Unloads all of the crew currently loaded, ready to start again if needed
	 */
	@Override
	public void reset() {
		route.clear();

	}

}
