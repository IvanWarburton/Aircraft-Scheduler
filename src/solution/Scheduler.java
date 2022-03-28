package solution;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;

import baseclasses.Aircraft;
import baseclasses.CabinCrew;
import baseclasses.Crew;
import baseclasses.DoubleBookedException;
import baseclasses.FlightInfo;
import baseclasses.IAircraftDAO;
import baseclasses.ICrewDAO;
import baseclasses.IPassengerNumbersDAO;
import baseclasses.IRouteDAO;
import baseclasses.IScheduler;
import baseclasses.InvalidAllocationException;
import baseclasses.Pilot;
import baseclasses.Pilot.Rank;
import baseclasses.Schedule;
import baseclasses.SchedulerRunner;

public class Scheduler implements IScheduler {

	Schedule theScheg;


	//The scheduler is not as commented as much as it could be as i ran out of time as i leave commenting till i have complete my code(I know its bad practice).
	//I have made the program gradely adding parts to make improvements on my score.
	//I got the programs current score isolated down to (Mostly) only needing to give return flight crews a rest for 12 hours.
	//Unfortunately my method for fixing the crew rest issue(commented out) did not implement into my code how i liked and ended up breaking everything.
	//If given more time i think i would be able to get my score below 100 million.
	@Override
	public Schedule generateSchedule(IAircraftDAO arg0, ICrewDAO arg1, IRouteDAO arg2, IPassengerNumbersDAO arg3, LocalDate arg4, LocalDate arg5) {

		theScheg = new Schedule(arg2, arg4, arg5);
		theScheg.sort();

		LocalTime TimeWorked;
		HashMap<String, LocalTime> TimeSheet = new HashMap<>();
		HashMap<Integer, FlightInfo> returnflight = new HashMap<>();

		ArrayList<Aircraft> ReturnPlane = new ArrayList<>();
		ArrayList<Crew> ReturnCrew = new ArrayList<>();

		ArrayList<LocalDateTime> CrewRestStart = new ArrayList<>();

		HashMap<LocalDateTime, ArrayList<Crew>> OnRest = new HashMap<>();

		ArrayList<Crew> TempRestCrew = new ArrayList<>();
		ArrayList<Crew> RestCrew = new ArrayList<>();
		


		while(theScheg.getRemainingAllocations().size()  != 0){		
			//Allocate Aircraft

			//reset time worked
			TimeWorked = LocalTime.parse("00:00:00");
			//The flight working on
			FlightInfo flight = theScheg.getRemainingAllocations().get(0);
			//gets predicted passenger number
			int PassNum = arg3.getPassengerNumbersFor(flight.getFlight().getFlightNumber(), LocalDate.parse((flight.getDepartureDateTime()).toString().substring(0,10)));
			//get starting position of flight
			String startPos = flight.getFlight().getDepartureAirportCode();
			//Use for planes that are not usable as no crew are qualified to fly
			ArrayList<Aircraft> BlackListed = new ArrayList<>();


			//check if crew finished resting and pull out of OnRest array
			for(int i=0;i<CrewRestStart.size();i++) {
				if(flight.getDepartureDateTime().isBefore(CrewRestStart.get(i).plusHours(12))) 
				{
					RestCrew.removeAll(OnRest.get(CrewRestStart.get(i)));
					OnRest.remove(CrewRestStart.get(i));
					CrewRestStart.remove(i);
				}
			}

			if(returnflight.containsKey(flight.getFlight().getFlightNumber())) 
			{
				FlightInfo rf = returnflight.get(flight.getFlight().getFlightNumber());
				try {
					theScheg.allocateAircraftTo(theScheg.getAircraftFor(rf), flight);
				} catch (DoubleBookedException e) {}

				try {
					theScheg.allocateCaptainTo(theScheg.getCaptainOf(rf), flight);
				} catch (DoubleBookedException e) {}

				try {
					theScheg.allocateFirstOfficerTo(theScheg.getFirstOfficerOf(rf), flight);;
				} catch (DoubleBookedException e) {}

				for(int i=0; i<theScheg.getCabinCrewOf(rf).size();i++)
				{
					try {
						theScheg.allocateCabinCrewTo(theScheg.getCabinCrewOf(rf).get(i), flight);
					} catch (DoubleBookedException e) {}
				}

				try {
					theScheg.completeAllocationFor(flight);

					TempRestCrew.add(theScheg.getCaptainOf(rf));
					TempRestCrew.add(theScheg.getFirstOfficerOf(rf));
					TempRestCrew.addAll(theScheg.getCabinCrewOf(rf));

					CrewRestStart.add(flight.getLandingDateTime());

					OnRest.put(flight.getLandingDateTime(),TempRestCrew);

					RestCrew.addAll(TempRestCrew);

					TempRestCrew.clear();
				} catch (InvalidAllocationException e) {}

				returnflight.remove(flight.getFlight().getFlightNumber());

				ReturnPlane.remove(theScheg.getAircraftFor(rf));
				ReturnCrew.remove(theScheg.getCaptainOf(rf));
				ReturnCrew.remove(theScheg.getFirstOfficerOf(rf));
				ReturnCrew.removeAll(theScheg.getCabinCrewOf(rf));

			}else
			{
				//Loop once for the first time then will rerun if plane added to BlackListed
				for(int x=0;x<BlackListed.size()+1;x++)
				{
					//Unallocated flight if previously allocated
					if(theScheg.getAircraftFor(flight) != null) 
						theScheg.unAllocate(flight);

					//get how many planes are available, Check if plane is BlackListed, Check if there in the correct starting position, check if plane is available for use, Allocate aircraft to flight
					for(int j=0;j<arg0.findAircraftBySeats(PassNum).size();j++)
					{
						if(arg0.findAircraftBySeats(PassNum).get(j).getStartingPosition().equals(startPos)) 
						{
							if(!BlackListed.contains(arg0.findAircraftBySeats(PassNum).get(j))) 
							{
								if(!ReturnPlane.contains(arg0.findAircraftBySeats(PassNum).get(j))) {
									if(!theScheg.hasConflict(arg0.findAircraftBySeats(PassNum).get(j), flight))
									{
										try {
											theScheg.allocateAircraftTo(arg0.findAircraftBySeats(PassNum).get(j), flight);
											break;
										} catch (DoubleBookedException e) {}
									}
								}
							}
						}
					}

					//No aircraft was allocated previous so now check all planes for a usable plane
					//Same as previous but only looking for planes with correct seats
					if(theScheg.getAircraftFor(flight) == null) 
					{
						for(int j=0;j<arg0.findAircraftBySeats(PassNum).size();j++)
						{
							if(!BlackListed.contains(arg0.findAircraftBySeats(PassNum).get(j))) 
							{
								if(!ReturnPlane.contains(arg0.findAircraftBySeats(PassNum).get(j))) {
									if(!theScheg.hasConflict(arg0.findAircraftBySeats(PassNum).get(j), flight))
									{
										try {
											theScheg.allocateAircraftTo(arg0.findAircraftBySeats(PassNum).get(j), flight);
											break;
										} catch (DoubleBookedException e) {}
									}
								}
							}
						}
					}

					//same as above but reducing seats till viable if still not allocated blacklist
					while(theScheg.getAircraftFor(flight) == null) {
						PassNum = PassNum - 10;
						if(PassNum <=-1)
							break;
						for(int j=0;j<arg0.findAircraftBySeats(PassNum).size();j++)
						{
							if(!BlackListed.contains(arg0.findAircraftBySeats(PassNum).get(j))) 
							{
								if(!ReturnPlane.contains(arg0.findAircraftBySeats(PassNum).get(j))) {
									if(!theScheg.hasConflict(arg0.findAircraftBySeats(PassNum).get(j), flight))
									{
										try {
											theScheg.allocateAircraftTo(arg0.findAircraftBySeats(PassNum).get(j), flight);
											break;
										} catch (DoubleBookedException e) {}
									}
									else
									{
										BlackListed.add(arg0.findAircraftBySeats(PassNum).get(j));
									}
								}
							}
						}
					}


					//Allocate Captain

					//if was unable to allocate aircraft because no captain qualified to fly available aircraft
					if(BlackListed.size() + ReturnPlane.size() >= arg0.getAllAircraft().size()) 
					{
						// reset passenger numbers previously changed
						PassNum = arg3.getPassengerNumbersFor(flight.getFlight().getFlightNumber(), LocalDate.parse((flight.getDepartureDateTime()).toString().substring(0,10)))+10;

						ReturnCrew.clear();
						ReturnPlane.clear();

						//search threw all aircraft till find viable craft
						while(theScheg.getAircraftFor(flight) == null) {
							if(PassNum <=-2)
								break;
							PassNum = PassNum - 10;
							for(int j=0;j<arg0.findAircraftBySeats(PassNum).size();j++)
							{	
								if(!theScheg.hasConflict(arg0.findAircraftBySeats(PassNum).get(j), flight))
								{
									try {
										theScheg.allocateAircraftTo(arg0.findAircraftBySeats(PassNum).get(j), flight);
										break;
									} catch (DoubleBookedException e) {}
								}
							}
						}

						//search all pilots to find first officer as captain(last option)
						ArrayList<Pilot> allPilot = new ArrayList<>(arg1.getAllPilots());
						for(int i=0; i<allPilot.size();i++)
						{
							if(!theScheg.hasConflict(allPilot.get(i), flight)) 
							{
								try {
									theScheg.allocateCaptainTo(allPilot.get(i), flight);
									break;
								} catch (DoubleBookedException e) {}
							}
						}

					}
					// Continue allocating captain
					else {

						//all pilots by home base and type rating
						ArrayList<Pilot> pilot = new ArrayList<>(arg1.findPilotsByHomeBaseAndTypeRating(theScheg.getAircraftFor(flight).getTypeCode(),startPos));		

						//allocate a captain for the flight
						for(int i=0; i<pilot.size();i++)
						{
							if(pilot.get(i).getRank() == Rank.CAPTAIN) 
							{
								// if TimeSheet has not been created yet employee has not yet worked
								if(TimeSheet.containsKey(pilot.get(i).getSurname()+pilot.get(i).getForename())) 
								{
									//if the current flight minutes plus existing worked minutes divided by 60 is less than the 100 monthly allowed work hours
									if(TimeSheet.get(pilot.get(i).getSurname()+pilot.get(i).getForename()).plusMinutes(flight.getFlight().getDuration().toMinutes()).getHour() < 100) 
									{
										if(!RestCrew.contains(pilot.get(i))) {
										if(!ReturnCrew.contains(pilot.get(i))) {
											if(!theScheg.hasConflict(pilot.get(i), flight)) 
											{
												try {
													theScheg.allocateCaptainTo(pilot.get(i), flight);
													break;
												} catch (DoubleBookedException e) {}
											}
										}
										}
									}
								}
								else 
								{
									if(!ReturnCrew.contains(pilot.get(i))) {
										if(!theScheg.hasConflict(pilot.get(i), flight)) 
										{
											try {
												theScheg.allocateCaptainTo(pilot.get(i), flight);
												break;
											} catch (DoubleBookedException e) {}
										}
									}
								}
							}
						}


						if(theScheg.getCaptainOf(flight) == null) 
						{
							ArrayList<Pilot> oocPilot = new ArrayList<>(arg1.findPilotsByTypeRating(theScheg.getAircraftFor(flight).getTypeCode()));
							for(int i=0; i<oocPilot.size();i++)
							{
								if(oocPilot.get(i).getRank() == Rank.CAPTAIN) 
								{
									//if TimeSheet has not been created yet employee has not yet worked
									if(TimeSheet.containsKey(oocPilot.get(i).getSurname()+oocPilot.get(i).getForename())) 
									{
										//if the current flight minutes plus existing worked minutes divided by 60 is less than the 100 monthly allowed work hours
										if(TimeSheet.get(oocPilot.get(i).getSurname()+oocPilot.get(i).getForename()).plusMinutes(flight.getFlight().getDuration().toMinutes()).getHour() < 100) 
										{
											if(!ReturnCrew.contains(oocPilot.get(i))) {
												if(!theScheg.hasConflict(oocPilot.get(i), flight)) 
												{
													try {
														theScheg.allocateCaptainTo(oocPilot.get(i), flight);
														break;
													} catch (DoubleBookedException e) {}
												}
											}
										}
									}else 
									{
										if(!ReturnCrew.contains(oocPilot.get(i))) {
											if(!theScheg.hasConflict(oocPilot.get(i), flight)) 
											{
												try {
													theScheg.allocateCaptainTo(oocPilot.get(i), flight);
													break;
												} catch (DoubleBookedException e) {}
											}
										}
									}
								}
							}
							//if still not allocated plane is not operational so blacklist
							if(theScheg.getCaptainOf(flight) == null)
								BlackListed.add(theScheg.getAircraftFor(flight));
						}	
					}
				}


				//if plane is usable move into position if needed
				if(theScheg.getAircraftFor(flight) != null) 
				{
					//Move Aircraft into position
					theScheg.getAircraftFor(flight).getStartingPosition();
					theScheg.getAircraftFor(flight).setStartingPosition(startPos);
				}




				//all pilots by home base and type rating
				ArrayList<Pilot> pilot = new ArrayList<>(arg1.findPilotsByHomeBaseAndTypeRating(theScheg.getAircraftFor(flight).getTypeCode(),startPos));

				//Allocate first officer	
				for(int i=0; i<pilot.size();i++)
				{
					if(pilot.get(i).getRank() == Rank.FIRST_OFFICER) 
					{
						// if TimeSheet has not been created yet employee has not yet worked
						if(TimeSheet.containsKey(pilot.get(i).getSurname()+pilot.get(i).getForename())) 
						{
							//if the current flight minutes plus existing worked minutes divided by 60 is less than the 100 monthly allowed work hours
							if(TimeSheet.get(pilot.get(i).getSurname()+pilot.get(i).getForename()).plusMinutes(flight.getFlight().getDuration().toMinutes()).getHour() < 100) 
							{
								if(!RestCrew.contains(pilot.get(i))) {
								if(!ReturnCrew.contains(pilot.get(i))) {
									if(!theScheg.hasConflict(pilot.get(i), flight)) 
									{
										try {
											theScheg.allocateFirstOfficerTo(pilot.get(i), flight);
											break;
										} catch (DoubleBookedException e) {}
									}
								}
								}
							}
						}else 
						{

							if(!ReturnCrew.contains(pilot.get(i))) {
								if(!theScheg.hasConflict(pilot.get(i), flight)) 
								{
									try {
										theScheg.allocateFirstOfficerTo(pilot.get(i), flight);
										break;
									} catch (DoubleBookedException e) {}
								}
							}
						}
					}
				}


				if(theScheg.getFirstOfficerOf(flight) == null) 
				{
					ArrayList<Pilot> oocPilot = new ArrayList<>(arg1.findPilotsByTypeRating(theScheg.getAircraftFor(flight).getTypeCode()));
					for(int i=0; i<oocPilot.size();i++)
					{
						if(oocPilot.get(i).getRank() == Rank.FIRST_OFFICER) 
						{
							// if TimeSheet has not been created yet employee has not yet worked
							if(TimeSheet.containsKey(oocPilot.get(i).getSurname()+oocPilot.get(i).getForename())) 
							{
								//if the current flight minutes plus existing worked minutes divided by 60 is less than the 100 monthly allowed work hours
								if(TimeSheet.get(oocPilot.get(i).getSurname()+oocPilot.get(i).getForename()).plusMinutes(flight.getFlight().getDuration().toMinutes()).getHour() < 100) 
								{
									if(!ReturnCrew.contains(oocPilot.get(i))) {
										if(!theScheg.hasConflict(oocPilot.get(i), flight)) 
										{
											try {
												theScheg.allocateFirstOfficerTo(oocPilot.get(i), flight);
												break;
											} catch (DoubleBookedException e) {}
										}
									}
								}
							}else 
							{
								if(!ReturnCrew.contains(oocPilot.get(i))) {
									if(!theScheg.hasConflict(oocPilot.get(i), flight)) 
									{
										try {
											theScheg.allocateFirstOfficerTo(oocPilot.get(i), flight);
											break;
										} catch (DoubleBookedException e) {}
									}
								}
							}
						}
					}

					if(theScheg.getFirstOfficerOf(flight) == null) 
					{
						ArrayList<Pilot> allPilot = new ArrayList<>(arg1.getAllPilots());
						for(int i=0; i<allPilot.size();i++)
						{
							if(!theScheg.hasConflict(allPilot.get(i), flight)) 
							{
								try {
									theScheg.allocateFirstOfficerTo(allPilot.get(i), flight);
									break;
								} catch (DoubleBookedException e) {}
							}
						}
					}
				}

				//Allocate Cabin Crew

				//all crew by home base and type rating
				ArrayList<CabinCrew> crw = new ArrayList<>(arg1.findCabinCrewByHomeBaseAndTypeRating(theScheg.getAircraftFor(flight).getTypeCode(), startPos));

				//get required crew for aircraft
				int requiredCrew = theScheg.getAircraftFor(flight).getCabinCrewRequired();

				//allocate crew required for plane to operate
				for(int i=0; i<requiredCrew;i++) 
				{
					for(int j=0; j<crw.size();j++)
					{
						// if TimeSheet has not been created yet employee has not yet worked
						if(TimeSheet.containsKey(crw.get(j).getSurname()+crw.get(j).getForename())) 
						{
							//if the current flight minutes plus existing worked minutes divided by 60 is less than the 100 monthly allowed work hours
							if(TimeSheet.get(crw.get(j).getSurname()+crw.get(j).getForename()).plusMinutes(flight.getFlight().getDuration().toMinutes()).getHour() < 100) 
							{
								if(!RestCrew.contains(crw.get(j))) {
								if(!ReturnCrew.contains(crw.get(j))) {
									if(!theScheg.hasConflict(crw.get(j), flight)) 
									{
										try {
											theScheg.allocateCabinCrewTo(crw.get(j), flight);
											break;
										} catch (DoubleBookedException e) {}
									}
								}
								}
							}
						}else 
						{
							if(!ReturnCrew.contains(crw.get(j))) {
								if(!theScheg.hasConflict(crw.get(j), flight)) 
								{
									try {
										theScheg.allocateCabinCrewTo(crw.get(j), flight);
										break;
									} catch (DoubleBookedException e) {}
								}
							}
						}
					}


				}
				//If unable to find crew required at previous find any crew
				if(theScheg.getCabinCrewOf(flight).size() < requiredCrew) 
				{
					ArrayList<CabinCrew> oocCrw = new ArrayList<>(arg1.findCabinCrewByTypeRating(theScheg.getAircraftFor(flight).getTypeCode()));
					for(int i=0; i<requiredCrew;i++) 
					{
						for(int j=0; j<oocCrw.size();j++)
						{
							// if TimeSheet has not been created yet employee has not yet worked
							if(TimeSheet.containsKey(oocCrw.get(j).getSurname()+oocCrw.get(j).getForename())) 
							{
								//if the current flight minutes plus existing worked minutes divided by 60 is less than the 100 monthly allowed work hours
								if(TimeSheet.get(oocCrw.get(j).getSurname()+oocCrw.get(j).getForename()).plusMinutes(flight.getFlight().getDuration().toMinutes()).getHour() < 100)
								{
									if(!ReturnCrew.contains(oocCrw.get(j))) {
										if(!theScheg.hasConflict(oocCrw.get(j), flight)) 
										{
											try {
												theScheg.allocateCabinCrewTo(oocCrw.get(j), flight);
												break;
											} catch (DoubleBookedException e) {}
										}
									}
								}
							}else 
							{
								if(!ReturnCrew.contains(oocCrw.get(j))) {
									if(!theScheg.hasConflict(oocCrw.get(j), flight)) 
									{
										try {
											theScheg.allocateCabinCrewTo(oocCrw.get(j), flight);
											break;
										} catch (DoubleBookedException e) {}
									}
								}
							}
						}
					}
				}

				//Complete Allocation
				try {
					theScheg.completeAllocationFor(flight);
				} catch (InvalidAllocationException e) {}

				System.out.println(flight.getDepartureDateTime());

				if(theScheg.getAircraftFor(flight) != null && theScheg.getCaptainOf(flight) != null && theScheg.getFirstOfficerOf(flight) != null && !theScheg.getCabinCrewOf(flight).isEmpty()) 
				{
					returnflight.put(flight.getFlight().getFlightNumber() +1, flight);
					ReturnPlane.add(theScheg.getAircraftFor(flight));
					ReturnCrew.add(theScheg.getCaptainOf(flight));
					ReturnCrew.add(theScheg.getFirstOfficerOf(flight));
					ReturnCrew.addAll(theScheg.getCabinCrewOf(flight));
				}
			}

			if(theScheg.getAircraftFor(flight) != null && theScheg.getCaptainOf(flight) != null && theScheg.getFirstOfficerOf(flight) != null && !theScheg.getCabinCrewOf(flight).isEmpty()) 
			{

				if(theScheg.getCabinCrewOf(flight).size() != theScheg.getAircraftFor(flight).getCabinCrewRequired()) 
				{
					TimeWorked = TimeWorked.plusMinutes(flight.getFlight().getDuration().toMinutes());

					//Reset starting positions of plane and crew to arrival airport
					theScheg.getAircraftFor(flight).setStartingPosition(flight.getFlight().getArrivalAirportCode());
					for(int i=0;i<theScheg.getCabinCrewOf(flight).size();i++)
					{
						theScheg.getCabinCrewOf(flight).get(i).setHomeBase(flight.getFlight().getArrivalAirportCode());
					}
					theScheg.getCaptainOf(flight).setHomeBase(flight.getFlight().getArrivalAirportCode());
					theScheg.getFirstOfficerOf(flight).setHomeBase(flight.getFlight().getArrivalAirportCode());

					//TimeSheet adding
					for(int i=0;i<theScheg.getCabinCrewOf(flight).size();i++)
					{
						if(TimeSheet.containsKey(theScheg.getCabinCrewOf(flight).get(i).getSurname()+theScheg.getCabinCrewOf(flight).get(i).getForename()))
							TimeWorked = TimeWorked.plusMinutes(TimeSheet.get(theScheg.getCabinCrewOf(flight).get(i).getSurname()+theScheg.getCabinCrewOf(flight).get(i).getForename()).getMinute());
						TimeSheet.put(theScheg.getCabinCrewOf(flight).get(i).getSurname()+theScheg.getCabinCrewOf(flight).get(i).getForename(), TimeWorked);
					}

					if(TimeSheet.containsKey(theScheg.getCaptainOf(flight).getSurname()+theScheg.getCaptainOf(flight).getForename()))
						TimeWorked = TimeWorked.plusMinutes(TimeSheet.get(theScheg.getCaptainOf(flight).getSurname()+theScheg.getCaptainOf(flight).getForename()).getMinute());
					TimeSheet.put(theScheg.getCaptainOf(flight).getSurname()+theScheg.getCaptainOf(flight).getForename(), TimeWorked);

					if(TimeSheet.containsKey(theScheg.getFirstOfficerOf(flight).getSurname()+theScheg.getFirstOfficerOf(flight).getForename()))
						TimeWorked = TimeWorked.plusMinutes(TimeSheet.get(theScheg.getFirstOfficerOf(flight).getSurname()+theScheg.getFirstOfficerOf(flight).getForename()).getMinute());
					TimeSheet.put(theScheg.getFirstOfficerOf(flight).getSurname()+theScheg.getFirstOfficerOf(flight).getForename(), TimeWorked);
				}
			}

		}

		System.out.println("Allocation Complete: " + theScheg.isCompleted());
		System.out.println("Allocated: " + theScheg.getCompletedAllocations().size());

		return theScheg;
	}

	@Override
	public void setSchedulerRunner(SchedulerRunner arg0) {	
	}

	@Override
	public void stop() {
	}

}
