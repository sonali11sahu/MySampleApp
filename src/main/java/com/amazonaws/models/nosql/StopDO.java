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

@DynamoDBTable(tableName = "myshuttleapp-mobilehub-389102477-Stop")

public class StopDO {
    private String _stopId;
    private String _stopLat;
    private String _stopLon;
    private String _stopName;
    public double distance = 0;

    @DynamoDBHashKey(attributeName = "stopId")
    @DynamoDBAttribute(attributeName = "stopId")
    public String getStopId() {
        return _stopId;
    }

    public void setStopId(final String _stopId) {
        this._stopId = _stopId;
    }
    @DynamoDBAttribute(attributeName = "stopLat")
    public String getStopLat() {
        return _stopLat;
    }

    public void setStopLat(final String _stopLat) {
        this._stopLat = _stopLat;
    }
    @DynamoDBAttribute(attributeName = "stopLon")
    public String getStopLon() {
        return _stopLon;
    }

    public void setStopLon(final String _stopLon) {
        this._stopLon = _stopLon;
    }
    @DynamoDBAttribute(attributeName = "stopName")
    public String getStopName() {
        return _stopName;
    }

    public void setStopName(final String _stopName) {
        this._stopName = _stopName;
    }

}
