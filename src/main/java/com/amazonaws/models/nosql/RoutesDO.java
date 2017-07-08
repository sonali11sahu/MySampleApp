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

@DynamoDBTable(tableName = "myshuttleapp-mobilehub-389102477-Routes")

public class RoutesDO {
    private String _routeId;
    private String _routeName;
    private String _stopSeq;

    @DynamoDBHashKey(attributeName = "routeId")
    @DynamoDBAttribute(attributeName = "routeId")
    public String getRouteId() {
        return _routeId;
    }

    public void setRouteId(final String _routeId) {
        this._routeId = _routeId;
    }
    @DynamoDBAttribute(attributeName = "routeName")
    public String getRouteName() {
        return _routeName;
    }

    public void setRouteName(final String _routeName) {
        this._routeName = _routeName;
    }
    @DynamoDBAttribute(attributeName = "stopSeq")
    public String getStopSeq() {
        return _stopSeq;
    }

    public void setStopSeq(final String _stopSeq) {
        this._stopSeq = _stopSeq;
    }

}
