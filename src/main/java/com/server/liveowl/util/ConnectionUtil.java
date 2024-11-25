package com.server.liveowl.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Component
public class ConnectionUtil {

@Autowired
private DataSource dataSource;

public Connection getConnection() throws Exception
{
    return dataSource.getConnection();
}

}
