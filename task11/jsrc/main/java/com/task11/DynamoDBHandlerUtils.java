package com.task11;

import java.time.format.DateTimeFormatter;

public class DynamoDBHandlerUtils {
    public static final String TABLES_TABLE_NAME = "cmtr-3477d8b3-Tables-test";
    public static final String RESERVATIONS_TABLE_NAME = "cmtr-3477d8b3-Reservations-test";
    public static final String RESERVATION_TABLE_ID_KEY = "id";
    public static final String RESERVATION_TABLE_TABLE_NUMBER_KEY = "tableNumber";
    public static final String RESERVATION_TABLE_DATE_KEY = "date";
    public static final String RESERVATION_TABLE_SLOT_TIME_START_KEY = "slotTimeStart";
    public static final String RESERVATION_TABLE_SLOT_TIME_END_KEY = "slotTimeEnd";
    public static final String RESERVATION_TABLE_CLIENT_NAME_KEY = "clientName";
    public static final String RESERVATION_TABLE_PHONE_NUMBER_KEY = "phoneNumber";
    public static final String TABLES_TABLE_ID_KEY = "id";
    public static final String TABLES_TABLE_NUMBER_KEY = "number";
    public static final String TABLES_TABLE_PLACES_KEY = "places";
    public static final String TABLES_TABLE_IS_VIP_KEY = "isVip";
    public static final String TABLES_TABLE_MIN_ORDER_KEY = "minOrder";
    static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
}
