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

@DynamoDBTable(tableName = "myshuttleapp-mobilehub-389102477-UserInfo")

public class UserInfoDO {
    private String _userId;
    private String _emailAddress;
    private String _givenName;
    private String _latitude;
    private String _longitude;
    private String _recentReportTime;

    @DynamoDBHashKey(attributeName = "userId")
    @DynamoDBAttribute(attributeName = "userId")
    public String getUserId() {
        return _userId;
    }

    public void setUserId(final String _userId) {
        this._userId = _userId;
    }
    @DynamoDBAttribute(attributeName = "emailAddress")
    public String getEmailAddress() {
        return _emailAddress;
    }

    public void setEmailAddress(final String _emailAddress) {
        this._emailAddress = _emailAddress;
    }

    @DynamoDBAttribute(attributeName = "givenName")
    public String getGivenName() {
        return _givenName;
    }

    public void setGivenName(final String _givenName) {
        this._givenName = _givenName;
    }
    @DynamoDBAttribute(attributeName = "latitude")
    public String getLatitude() {
        return _latitude;
    }

    public void setLatitude(final String _latitude) {
        this._latitude = _latitude;
    }
    @DynamoDBAttribute(attributeName = "longitude")
    public String getLongitude() {
        return _longitude;
    }

    public void setLongitude(final String _longitude) {
        this._longitude = _longitude;
    }


    @DynamoDBAttribute(attributeName = "recentReportTime")
    public String getRecentReportTime() {
        return _recentReportTime;
    }

    public void setRecentReportTime(final String _recentReportTime) {
        this._recentReportTime = _recentReportTime;
    }

}
