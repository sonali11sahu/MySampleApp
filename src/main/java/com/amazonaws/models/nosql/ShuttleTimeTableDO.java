package com.amazonaws.models.nosql;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.List;
import java.util.Map;
import java.util.Set;

@DynamoDBTable(tableName = "myshuttleapp-mobilehub-389102477-ShuttleTimeTable")

public class ShuttleTimeTableDO {
    private String _shuttleId;
    private String _time;
    private String _daysWorking;
    private String _stopId;

    @DynamoDBHashKey(attributeName = "shuttleId")
    @DynamoDBAttribute(attributeName = "shuttleId")
    public String getShuttleId() {
        return _shuttleId;
    }

    public void setShuttleId(final String _shuttleId) {
        this._shuttleId = _shuttleId;
    }
    @DynamoDBRangeKey(attributeName = "time")
    @DynamoDBAttribute(attributeName = "time")
    public String getTime() {
        return _time;
    }

    public void setTime(final String _time) {
        this._time = _time;
    }
    @DynamoDBAttribute(attributeName = "DaysWorking")
    public String getDaysWorking() {
        return _daysWorking;
    }

    public void setDaysWorking(final String _daysWorking) {
        this._daysWorking = _daysWorking;
    }
    @DynamoDBAttribute(attributeName = "stopId")
    public String getStopId() {
        return _stopId;
    }

    public void setStopId(final String _stopId) {
        this._stopId = _stopId;
    }

}
