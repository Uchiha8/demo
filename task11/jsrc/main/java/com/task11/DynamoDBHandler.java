package com.task11;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.ScanOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.task11.DynamoDBHandlerUtils.*;

public class DynamoDBHandler {

    private final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();
    private final DynamoDB dynamoDB = new DynamoDB(client);
    private static DynamoDBHandler instance;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private DynamoDBHandler() {

    }

    public static DynamoDBHandler getInstance() {
        if (instance == null) {
            instance = new DynamoDBHandler();
        }
        return instance;
    }

    protected APIGatewayProxyResponseEvent getItemsFromTables(Map<String, String> CORSHeaders) throws JsonProcessingException {
        System.out.println("Get Items < Tables > ");
        Table table = getTableByName(TABLES_TABLE_NAME);
        ScanSpec scanSpec = new ScanSpec();

        ItemCollection<ScanOutcome> items = table.scan(scanSpec);

        TableListResponse tableListResponse = new TableListResponse();
        Iterator<Item> iterator = items.iterator();
        while (iterator.hasNext()) {
            Item item = iterator.next();
            Map<String, Object> itemMap = item.asMap();
            System.out.println("Item: " + itemMap);
            CustomTableResponse tableItem = createTableFromMap(itemMap);
            tableListResponse.addTableItem(tableItem);
        }

        try {
            String tableResponseJson = objectMapper.writeValueAsString(tableListResponse);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withHeaders(CORSHeaders)
                    .withBody(tableResponseJson);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new APIGatewayProxyResponseEvent()
                    .withHeaders(CORSHeaders)
                    .withStatusCode(400);
        }

    }

    private CustomTableResponse createTableFromMap(Map<String, Object> itemMap) {
        CustomTableResponse customTableResponse = new CustomTableResponse();
        customTableResponse.setId(((BigDecimal) itemMap.get(TABLES_TABLE_ID_KEY)).intValue());
        customTableResponse.setNumber(((BigDecimal) itemMap.get(TABLES_TABLE_NUMBER_KEY)).intValue());
        customTableResponse.setPlaces(((BigDecimal) itemMap.get(TABLES_TABLE_PLACES_KEY)).intValue());
        customTableResponse.setVip((Boolean) itemMap.get(TABLES_TABLE_IS_VIP_KEY));
        customTableResponse.setMinOrder(((BigDecimal) itemMap.getOrDefault(TABLES_TABLE_MIN_ORDER_KEY, 0)).intValue());
        return customTableResponse;
    }

    protected APIGatewayProxyResponseEvent getItemsFromReservations(Map<String, String> CORSHeaders) throws JsonProcessingException {
        System.out.println("Get Items < Reservations > ");
        Table table = getTableByName(RESERVATIONS_TABLE_NAME);
        ScanSpec scanSpec = new ScanSpec();
        ReservationListResponse reservationListResponse = new ReservationListResponse();
        ItemCollection<ScanOutcome> items = table.scan(scanSpec);

        Iterator<Item> iterator = items.iterator();
        while (iterator.hasNext()) {
            Item item = iterator.next();
            Map<String, Object> itemMap = item.asMap();
            System.out.println("Item: " + itemMap);
            Reservation reservation = createReservationFromMap(itemMap);
            reservationListResponse.addReservationItem(reservation);
        }

        try {
            String responseBody = objectMapper.writeValueAsString(reservationListResponse);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withHeaders(CORSHeaders)
                    .withBody(responseBody);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new APIGatewayProxyResponseEvent()
                    .withHeaders(CORSHeaders)
                    .withStatusCode(400);
        }

    }

    private Reservation createReservationFromMap(Map<String, Object> itemMap) {
        Reservation reservation = new Reservation();
        reservation.setTableNumber(((BigDecimal) itemMap.get(RESERVATION_TABLE_TABLE_NUMBER_KEY)).intValue());
        reservation.setClientName((String) itemMap.get(RESERVATION_TABLE_CLIENT_NAME_KEY));
        reservation.setPhoneNumber((String) itemMap.get(RESERVATION_TABLE_PHONE_NUMBER_KEY));
        reservation.setDate((String) itemMap.get(RESERVATION_TABLE_DATE_KEY));
        reservation.setSlotTimeStart((String) itemMap.get(RESERVATION_TABLE_SLOT_TIME_START_KEY));
        reservation.setSlotTimeEnd((String) itemMap.get(RESERVATION_TABLE_SLOT_TIME_END_KEY));

        return reservation;
    }

    public APIGatewayProxyResponseEvent getItemFromTables(String id, Map<String, String> CORSHeaders) {
        System.out.println("Get Item < Tables > ");
        Table table = getTableByName(TABLES_TABLE_NAME);

        try {
            Integer idInt = Integer.parseInt(id);
            Item item = table.getItem(TABLES_TABLE_ID_KEY, idInt);
            return new APIGatewayProxyResponseEvent().withStatusCode(200).withHeaders(CORSHeaders).withBody(item.toJSON());
        } catch(Exception e) {
            e.printStackTrace();
            return new APIGatewayProxyResponseEvent().withStatusCode(400).withHeaders(CORSHeaders);
        }
    }

    public APIGatewayProxyResponseEvent putItemInTables(String body, Map<String, String> CORSHeaders) throws JsonProcessingException {
        System.out.println("Put Item < Tables > ");
        Map<String, Object> map = objectMapper.readValue(body, HashMap.class);

        Item tableItem = createTableItem(map);

        Table table = getTableByName(TABLES_TABLE_NAME);
        try {
            table.putItem(tableItem);
            int tableId = tableItem.getInt(TABLES_TABLE_ID_KEY);
            TableCreatedResponse tableCreatedResponse = new TableCreatedResponse(tableId);
            String responseBody = objectMapper.writeValueAsString(tableCreatedResponse);

            return new APIGatewayProxyResponseEvent().withStatusCode(200).withHeaders(CORSHeaders).withBody(responseBody);
        } catch (Exception e) {
            e.printStackTrace();
            return new APIGatewayProxyResponseEvent().withStatusCode(400).withHeaders(CORSHeaders);
        }
    }

    public APIGatewayProxyResponseEvent putItemInReservations(String body, Map<String, String> CORSHeaders) throws JsonProcessingException {
        System.out.println("Put Item < Reservations > ");
        Map<String, Object> map = objectMapper.readValue(body, HashMap.class);
        Item reservationItem = createReservationItem(map);

        if (!isValidReservationRequest(reservationItem) || !isTableExist(reservationItem.getNumber("tableNumber"))) {
            System.out.println("Validation failed < Put Item Reservation >");
            return new APIGatewayProxyResponseEvent().withStatusCode(400);
        }

        Table reservationTable = getTableByName(RESERVATIONS_TABLE_NAME);

        try {
            reservationTable.putItem(reservationItem);
            String responseBody = "{\"reservationId\": \"" + reservationItem.getString(RESERVATION_TABLE_ID_KEY) + "\"}";
            return new APIGatewayProxyResponseEvent().withStatusCode(200).withHeaders(CORSHeaders).withBody(responseBody);
        } catch (Exception e) {
            e.printStackTrace();
            return new APIGatewayProxyResponseEvent().withStatusCode(400).withHeaders(CORSHeaders);
        }
    }

    private boolean isTableExist(BigDecimal tableNumber) {

        System.out.println("isTableExist < Tables > ");
        Table table = getTableByName(TABLES_TABLE_NAME);
        ScanSpec scanSpec = new ScanSpec();

        ItemCollection<ScanOutcome> items = table.scan(scanSpec);
        boolean isTableExist = false;
        Iterator<Item> iterator = items.iterator();
        while (iterator.hasNext()) {
            Item item = iterator.next();
            BigDecimal currTableNumber = item.getNumber(TABLES_TABLE_NUMBER_KEY);
            if (currTableNumber.equals(tableNumber)) {
                isTableExist = true;
                break;
            }
        }
        System.out.println("Table with number " + tableNumber + " exist: " + "< " + isTableExist + " >");
        return isTableExist;
    }

    private boolean isValidReservationRequest(Item reservationItem) {

        BigDecimal requestTableNumber = reservationItem.getNumber(RESERVATION_TABLE_TABLE_NUMBER_KEY);
        LocalDate requestDate = formatDate( (String) reservationItem.get(RESERVATION_TABLE_DATE_KEY) );
        LocalTime requestSlotTimeStart = formatTime( (String) reservationItem.get(RESERVATION_TABLE_SLOT_TIME_START_KEY) );
        LocalTime requestSlotTimeEnd = formatTime( (String) reservationItem.get(RESERVATION_TABLE_SLOT_TIME_END_KEY) );


        System.out.println("Is Valid Request < Reservations > ");
        Table table = getTableByName(RESERVATIONS_TABLE_NAME);
        ScanSpec scanSpec = new ScanSpec();
        ItemCollection<ScanOutcome> items = table.scan(scanSpec);

        Iterator<Item> iterator = items.iterator();
        while (iterator.hasNext()) {
            Item item = iterator.next();
            Map<String, Object> itemMap = item.asMap();
            BigDecimal reservedTableNumber = (BigDecimal) itemMap.get(RESERVATION_TABLE_TABLE_NUMBER_KEY);
            LocalDate reservedDate = formatDate( (String) itemMap.get(RESERVATION_TABLE_DATE_KEY) );
            LocalTime reservedSlotTimeStart = formatTime( (String) itemMap.get(RESERVATION_TABLE_SLOT_TIME_START_KEY) );
            LocalTime reservedSlotTimeEnd = formatTime( (String) itemMap.get(RESERVATION_TABLE_SLOT_TIME_END_KEY) );

            if (reservedTableNumber.equals(requestTableNumber) && reservedDate.isEqual(requestDate)) {
                if ( isReservedTimeAfterRequestedTime(reservedSlotTimeStart, requestSlotTimeStart, requestSlotTimeEnd) ||
                     isReservedTimeBeforeRequestedTime(reservedSlotTimeEnd, requestSlotTimeStart, requestSlotTimeEnd) ) {
                    continue;
                }else {
                    System.out.println("Failed < isValidReservationRequest >");
                    System.out.println("Request Time: " + "< " + requestSlotTimeStart + ":" + requestSlotTimeEnd +" >");
                    System.out.println("Reserved Time: " + "< " + reservedSlotTimeStart + ":" + reservedSlotTimeEnd +" >");
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isReservedTimeBeforeRequestedTime(LocalTime reservedSlotTimeEnd, LocalTime requestSlotTimeStart, LocalTime requestSlotTimeEnd) {
        return ( reservedSlotTimeEnd.isBefore(requestSlotTimeStart) && reservedSlotTimeEnd.isBefore(requestSlotTimeEnd) ) ||
               ( reservedSlotTimeEnd.equals(requestSlotTimeStart) && reservedSlotTimeEnd.isBefore(requestSlotTimeEnd) );
    }

    private boolean isReservedTimeAfterRequestedTime(LocalTime reservedSlotTimeStart, LocalTime requestSlotTimeStart, LocalTime requestSlotTimeEnd) {
        return ( reservedSlotTimeStart.isAfter(requestSlotTimeStart) && reservedSlotTimeStart.isAfter(requestSlotTimeEnd) ) ||
               ( reservedSlotTimeStart.isAfter(requestSlotTimeStart) && reservedSlotTimeStart.equals(requestSlotTimeEnd) );
    }

    private LocalDate formatDate(String date) {
        return LocalDate.parse(date, DATE_FORMAT);
    }

    private LocalTime formatTime(String time) {
        return LocalTime.parse(time, TIME_FORMAT);
    }

    public Item createReservationItem(Map<String, Object> body) {
        System.out.println("Create Item < Reservation > ");

        Integer tableNumber = (Integer) body.get(RESERVATION_TABLE_TABLE_NUMBER_KEY);
        String clientName = (String) body.get(RESERVATION_TABLE_CLIENT_NAME_KEY);
        String phoneNumber = (String) body.get(RESERVATION_TABLE_PHONE_NUMBER_KEY);
        String date = (String) body.get(RESERVATION_TABLE_DATE_KEY);
        String slotTimeStart = (String) body.get(RESERVATION_TABLE_SLOT_TIME_START_KEY);
        String slotTimeEnd = (String) body.get(RESERVATION_TABLE_SLOT_TIME_END_KEY);

        return new Item()
                .withPrimaryKey(RESERVATION_TABLE_ID_KEY, UUID.randomUUID().toString())
                .withNumber(RESERVATION_TABLE_TABLE_NUMBER_KEY, tableNumber)
                .withString(RESERVATION_TABLE_CLIENT_NAME_KEY, clientName)
                .withString(RESERVATION_TABLE_PHONE_NUMBER_KEY, phoneNumber)
                .withString(RESERVATION_TABLE_DATE_KEY, date)
                .withString(RESERVATION_TABLE_SLOT_TIME_START_KEY, slotTimeStart)
                .withString(RESERVATION_TABLE_SLOT_TIME_END_KEY, slotTimeEnd);
    }
    private Item createTableItem(Map<String, Object> body) {
        System.out.println("Create Item < Table > ");

        Integer id = (Integer) body.get(TABLES_TABLE_ID_KEY);
        Integer numberOfTable = (Integer) body.get(TABLES_TABLE_NUMBER_KEY);
        Integer places = (Integer) body.get(TABLES_TABLE_PLACES_KEY);
        boolean isVip = (boolean) body.get(TABLES_TABLE_IS_VIP_KEY);
        Integer minOrder = (Integer) body.getOrDefault(TABLES_TABLE_MIN_ORDER_KEY,  null);

        Item item = new Item()
                .withPrimaryKey(TABLES_TABLE_ID_KEY, id)
                .withNumber(TABLES_TABLE_NUMBER_KEY, numberOfTable)
                .withNumber(TABLES_TABLE_PLACES_KEY, places)
                .withBoolean(TABLES_TABLE_IS_VIP_KEY, isVip);

        return minOrder != null ? item.withNumber(TABLES_TABLE_MIN_ORDER_KEY, minOrder) : item;
    }

    private Table getTableByName(String tableName) {
        try {
            return dynamoDB.getTable(tableName);
        }catch(Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }
}

class TableListResponse {
    private List<CustomTableResponse> tables;

    public TableListResponse() {
        this.tables = new ArrayList<>();
    }

    public void addTableItem(CustomTableResponse item) {
        this.tables.add(item);
    }

    public List<CustomTableResponse> getTables() {
        return tables;
    }

    public void setTables(List<CustomTableResponse> tables) {
        this.tables = tables;
    }

    @Override
    public String toString() {
        return "TableResponse{" +
                "tables=" + tables +
                '}';
    }
}

class ReservationListResponse {
    List<Reservation> reservations;

    public ReservationListResponse() {
        this.reservations = new ArrayList<>();
    }

    public void addReservationItem(Reservation item) {
        this.reservations.add(item);
    }

    public List<Reservation> getReservations() {
        return reservations;
    }

    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }

    @Override
    public String toString() {
        return "ReservationResponse{" +
                "reservations=" + reservations +
                '}';
    }
}
