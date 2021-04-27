//package eng.jAtcSim.newLib.traffic;
//
//import eng.eSystem.ERandom;
//import eng.eSystem.Tuple;
//import eng.eSystem.collections.EList;
//import eng.eSystem.collections.IList;
//import eng.eSystem.collections.IReadOnlyList;
//import eng.eSystem.eXml.XElement;
//import eng.eSystem.utilites.NumberUtils;
//import eng.jAtcSim.newLib.shared.SharedFactory;
//import eng.jAtcSim.newLib.shared.timeOld.ETimeStamp;
//import eng.jAtcSim.newLib.shared.xml.XmlLoader;
//import eng.jAtcSim.newLib.traffic.movementTemplating.CategoryMovementTemplate;
//import eng.jAtcSim.newLib.traffic.movementTemplating.EntryExitInfo;
//import eng.jAtcSim.newLib.traffic.movementTemplating.MovementTemplate;
//
///**
// * @author Marek Vajgl
// */
//public class GenericTraffic extends GeneratedTraffic {
//
//  public static GenericTraffic create(String[] companies, String[] countryCodes, int[] movementsPerHour,
//                                      double probabilityOfDeparture, double probabilityOfNonCommercialFlight,
//                                      double trafficCustomWeightTypeA, double trafficCustomWeightTypeB, double trafficCustomWeightTypeC, double trafficCustomWeightTypeD,
//                                      double delayProbability, int maxDelayInMinutesPerStep, boolean useExtendedCallsigns) {
//    GenericTraffic ret = new GenericTraffic(companies, countryCodes, movementsPerHour, probabilityOfDeparture, probabilityOfNonCommercialFlight, trafficCustomWeightTypeA, trafficCustomWeightTypeB, trafficCustomWeightTypeC, trafficCustomWeightTypeD, delayProbability, maxDelayInMinutesPerStep, useExtendedCallsigns);
//    return ret;
//  }
//
//  public static GenericTraffic load(XElement source) {
//    XmlLoader.setContext(source);
//    double delayProbability = XmlLoader.loadDouble("delayProbability");
//    int maxDelayInMinutesPerStep = XmlLoader.loadInteger("maxDelayInMinutesPerStep");
//    double probabilityOfNonCommercialFlight = XmlLoader.loadDouble("probabilityOfNonCommercialFlight");
//    double probabilityOfDeparture = XmlLoader.loadDouble("probabilityOfDeparture");
//    boolean useExtendedCallsigns = XmlLoader.loadBoolean("useExtendedCallsigns");
//
//    int[] mph = new int[24];
//    int i = 0;
//    for (XElement child : source.getChild("movementsPerHour").getChildren("item")) {
//      mph[i++] = Integer.parseInt(child.getContent());
//    }
//
//    double[] poc = new double[4];
//    i = 0;
//    for (XElement child : source.getChild("probabilityOfCategory").getChildren("item")) {
//      poc[i++] = Double.parseDouble(child.getContent());
//    }
//
//    IList<String> companies = new EList<>();
//    for (XElement child : source.getChild("companies").getChildren("item")) {
//      companies.add(child.getContent());
//    }
//
//    IList<String> countryCodes = new EList<>();
//    for (XElement child : source.getChild("countryCodes").getChildren("item")) {
//      countryCodes.add(child.getContent());
//    }
//
//    GenericTraffic ret = new GenericTraffic(
//        companies.toArray(String.class), countryCodes.toArray(String.class),
//        mph, probabilityOfDeparture, probabilityOfNonCommercialFlight,
//        poc[0], poc[1], poc[2], poc[3],
//        delayProbability, maxDelayInMinutesPerStep, useExtendedCallsigns);
//    return ret;
//  }
//
//
//  private ERandom rnd = null;
//
////  @Override
////  public GeneratedMovementsResponse generateMovements(Object syncObject) {
////    IList<Movement> lst = new EList<>();
////
////    int currentHour = Acc.now().getHours();
////    int expMovs = movementsPerHour[currentHour];
////    for (int i = 0; i < expMovs; i++) {
////      Movement m = generateMovement(currentHour);
////      lst.add(m);
////    }
////
////    // what will this to over midnight?
////    ETime nextGenTime = Acc.now().getRoundedToNextHour();
////
////    GeneratedMovementsResponse ret = new GeneratedMovementsResponse(
////        nextGenTime, null, lst);
////    return ret;
////  }
//
//  private GenericTraffic(String[] companies, String[] countryCodes, int[] movementsPerHour,
//                         double probabilityOfDeparture, double probabilityOfNonCommercialFlight,
//                         double trafficCustomWeightTypeA, double trafficCustomWeightTypeB, double trafficCustomWeightTypeC, double trafficCustomWeightTypeD,
//                         double delayProbability, int maxDelayInMinutesPerStep, boolean useExtendedCallsigns) {
//    super(delayProbability, maxDelayInMinutesPerStep, useExtendedCallsigns);
//
//    if (movementsPerHour == null) {
//      throw new IllegalArgumentException("Value of {movementsPerHour} cannot not be null.");
//    }
//    if (movementsPerHour.length != 24) {
//      throw new IllegalArgumentException("Argument \"movementsPerHour\" must have length = 24.");
//    }
//
//    for (int i = 0; i < movementsPerHour.length; i++) {
//      if (movementsPerHour[i] < 0)
//        throw new IllegalArgumentException("Argument \"movementsPerHour\" must have all elements equal or greater than 0.");
//    }
//
//    if (NumberUtils.isBetweenOrEqual(0, probabilityOfDeparture, 1) == false) {
//      throw new IllegalArgumentException("\"probabilityOfDeparture\" must be between 0 and 1.");
//    }
//
//    this.companies = companies;
//    this.countryCodes = countryCodes;
//
//    for (int i = 0; i < this.movementsPerHour.length; i++) {
//      this.movementsPerHour[i] = movementsPerHour[i];
//    }
//
//    this.probabilityOfDeparture = probabilityOfDeparture;
//    this.probabilityOfNonCommercialFlight = probabilityOfNonCommercialFlight;
//
//    // category probabilities init
//    {
//      double sum = trafficCustomWeightTypeA + trafficCustomWeightTypeB + trafficCustomWeightTypeC + trafficCustomWeightTypeD;
//      probabilityOfCategory[0] = trafficCustomWeightTypeA / sum;
//      probabilityOfCategory[1] = trafficCustomWeightTypeB / sum;
//      probabilityOfCategory[2] = trafficCustomWeightTypeC / sum;
//      probabilityOfCategory[3] = trafficCustomWeightTypeD / sum;
//    }
//
//    this.orderedCategoriesByProbabilityDesc = new char[4];
//    this.fillOrderedCategories();
//  }
//
//  @Override
//  public IReadOnlyList<ExpectedMovement> getExpectedTimesForDay() {
//    ERandom rnd = SharedFactory.getRnd();
//    IList<ExpectedMovement> ret = new EList<>();
//    for (int i = 0; i < 24; i++) {
//      for (int j = 0; j < movementsPerHour[i]; j++) {
//        ETimeStamp time = new ETimeStamp(i, rnd.nextInt(0, 60), rnd.nextInt(0, 60));
//        boolean isArrival = this.probabilityOfDeparture < rnd.nextDouble();
//        boolean isCommercial = this.probabilityOfNonCommercialFlight > rnd.nextDouble();
//        char category = getRandomCategory();
//        ExpectedMovement em = new ExpectedMovement(time, isArrival, isCommercial, category);
//        ret.add(em);
//      }
//    }
//    return ret;
//  }
//
//  @Override
//  public IReadOnlyList<MovementTemplate> getMovements(ETimeStamp fromTimeInclusive, ETimeStamp toTimeExclusive) {
//    return null;
//  }
//
//  private void fillOrderedCategories() {
//    IList<Tuple<Character, Double>> tmp = new EList<>();
//    tmp.add(new Tuple<>('A', probabilityOfCategory[0]));
//    tmp.add(new Tuple<>('B', probabilityOfCategory[1]));
//    tmp.add(new Tuple<>('C', probabilityOfCategory[2]));
//    tmp.add(new Tuple<>('D', probabilityOfCategory[3]));
//    tmp.sort(q -> -q.getB());
//
//    for (int i = 0; i < tmp.size(); i++) {
//      this.orderedCategoriesByProbabilityDesc[i] = tmp.get(i).getA();
//    }
//  }
//
//  private CategoryMovementTemplate generateMovement(int hour) {
//    ERandom rnd = SharedFactory.getRnd();
//    ETimeStamp initTime = new ETimeStamp(hour, rnd.nextInt(0, 60), rnd.nextInt(0, 60));
//    MovementTemplate.eKind kind = (rnd.nextDouble() <= this.probabilityOfDeparture) ? MovementTemplate.eKind.departure : MovementTemplate.eKind.arrival;
//    boolean isNonCommercial = rnd.nextDouble() < this.probabilityOfNonCommercialFlight;
//    char category = getRandomCategory();
//    int delayInMinutes = generateDelayMinutes();
//    int entryRadial = rnd.nextInt(360);
//
//    CategoryMovementTemplate ret = new CategoryMovementTemplate(
//        category, !isNonCommercial, kind, initTime, delayInMinutes, new EntryExitInfo(entryRadial)
//    );
//    return ret;
//  }
//
////  private Tuple<String, AirplaneType> getCompanyPrefixAndAirplaneType(char category) {
////    CompanyFleet companyFleet = null;
////    String icao = null;
////    AirplaneType type = null;
////
////    IList<CompanyFleet> flts = Acc.fleets().getCompaniesByIcao(this.companies);
////
////    // this will try restrict to required category
////    IList<CompanyFleet> tmp = flts.where(q -> q.getTypes().isAny(p -> p.getAirplaneType().category == category));
////    if (tmp.isEmpty()) {
////      if (this.orderedCategoriesByProbabilityDesc == null) fillOrderedCategories();
////      for (char c : this.orderedCategoriesByProbabilityDesc) {
////        tmp = flts.where(q -> q.getTypes().isAny(p -> p.getAirplaneType().category == c));
////        if (!tmp.isEmpty()) {
////          companyFleet = tmp.getRandom();
////          icao = companyFleet.getIcao();
////          type = companyFleet.getTypes().where(q -> q.getAirplaneType().category == c).getRandom().getAirplaneType();
////          break;
////        }
////      }
////      if (companyFleet == null) {
////        companyFleet = flts.getRandom();
////        icao = companyFleet.getIcao();
////        type = companyFleet.getTypes().getRandom().getAirplaneType();
////      }
////      if (companyFleet == null)
////        throw new ApplicationException("There is no plane kind matching requested category and company.");
////    } else {
////      companyFleet = tmp.getRandom();
////      icao = companyFleet.getIcao();
////      type = companyFleet.getTypes().where(q -> q.getAirplaneType().category == category).getRandom().getAirplaneType();
////    }
////
////    //those should be set
////    assert icao != null;
////    assert type != null;
////
////    Tuple<String, AirplaneType> ret = new Tuple<>(icao, type);
////    return ret;
////  }
//
//  private char getRandomCategory() {
//    char ret = 'A';
//    double sum = 0;
//    for (double v : probabilityOfCategory) {
//      sum += v;
//    }
//    double tmp = getRnd().nextDouble(sum);
//    int index = -1;
//    while (index < probabilityOfCategory.length) {
//      index++;
//      if (tmp < probabilityOfCategory[index]) {
//        ret = (char) ((int) ret + index);
//        break;
//      } else {
//        tmp -= probabilityOfCategory[index];
//      }
//    }
//    return ret;
//  }
//
//  private ERandom getRnd() {
//    if (rnd == null)
//      this.rnd = SharedFactory.getRnd();
//    return rnd;
//  }
//}
//
