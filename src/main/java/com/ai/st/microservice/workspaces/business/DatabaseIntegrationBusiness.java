package com.ai.st.microservice.workspaces.business;

import com.ai.st.microservice.common.exceptions.BusinessException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

@Component
public class DatabaseIntegrationBusiness {

    private final Logger log = LoggerFactory.getLogger(DatabaseIntegrationBusiness.class);

    @Value("${integrations.database.hostname}")
    private String databaseHost;

    @Value("${integrations.database.port}")
    private String databasePort;

    @Value("${integrations.database.username}")
    private String databaseUsername;

    @Value("${integrations.database.password}")
    private String databasePassword;

    public boolean createDatabase(String database, String username, String password) throws BusinessException {

        try {

            String url = "jdbc:postgresql://" + databaseHost + ":" + databasePort + "/postgres";

            Connection connection = DriverManager.getConnection(url, databaseUsername, databasePassword);

            PreparedStatement stmt1 = connection.prepareStatement("create database " + database);
            stmt1.execute();

            PreparedStatement stmt2 = connection
                    .prepareStatement("create user " + username + " with encrypted password '" + password
                            + "' NOSUPERUSER INHERIT NOCREATEDB NOCREATEROLE NOREPLICATION");
            stmt2.execute();

            log.info("DATABASE: " + database + " USER: " + username + " PASSWORD: " + password);

            this.createExtensionsToDatabase(database);

        } catch (Exception e) {
            log.error("Error creando base de datos: " + e.getMessage());
            throw new BusinessException("No se ha podido generar la base de datos.");
        }

        return true;
    }

    public void protectedDatabase(String host, String port, String database, String schema, String username,
                                  String password) throws BusinessException {

        try {

            String url = "jdbc:postgresql://" + host + ":" + port + "/" + database;

            Connection connection = DriverManager.getConnection(url, databaseUsername, databasePassword);

            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("select max(t.t_id) from " + schema + ".ini_predioinsumos t");
            rs.next();
            long maxId = rs.getLong(1);

            PreparedStatement stmt5 = connection
                    .prepareStatement("GRANT ALL ON SEQUENCE " + schema + ".t_ili2db_seq TO " + username);
            stmt5.execute();

            PreparedStatement stmt1 = connection
                    .prepareStatement("ALTER TABLE " + schema + ".ini_predioinsumos OWNER TO " + databaseUsername);
            stmt1.execute();

            PreparedStatement stmt2 = connection
                    .prepareStatement("GRANT USAGE ON SCHEMA " + schema + " TO " + username);
            stmt2.execute();

            PreparedStatement stmt3 = connection
                    .prepareStatement("GRANT SELECT ON ALL TABLES IN SCHEMA " + schema + " TO " + username);
            stmt3.execute();

            PreparedStatement stmt4 = connection.prepareStatement(
                    "GRANT INSERT, UPDATE, DELETE ON " + schema + ".ini_predioinsumos TO " + username);
            stmt4.execute();

            PreparedStatement stmt6 = connection
                    .prepareStatement("ALTER TABLE " + schema + ".ini_predioinsumos ENABLE ROW LEVEL SECURITY");
            stmt6.execute();

            PreparedStatement stmt7 = connection.prepareStatement(
                    "CREATE POLICY all_users_policy ON " + schema + ".ini_predioinsumos USING (false)");
            stmt7.execute();

            PreparedStatement stmt8 = connection.prepareStatement("CREATE POLICY all_users_select_policy ON " + schema
                    + ".ini_predioinsumos FOR SELECT USING (true)");
            stmt8.execute();

            PreparedStatement stmt9 = connection.prepareStatement("CREATE POLICY all_users_delete_policy ON " + schema
                    + ".ini_predioinsumos FOR DELETE USING (t_id > " + maxId + ")");
            stmt9.execute();

            PreparedStatement stmt10 = connection.prepareStatement("CREATE POLICY all_users_update_policy ON " + schema
                    + ".ini_predioinsumos FOR UPDATE WITH CHECK (t_id > " + maxId + ")");
            stmt10.execute();

            PreparedStatement stmt11 = connection.prepareStatement("CREATE POLICY all_users_insert_policy ON " + schema
                    + ".ini_predioinsumos FOR INSERT WITH CHECK (t_id > " + maxId + ")");
            stmt11.execute();

            PreparedStatement stmt12 = connection.prepareStatement("ALTER TABLE " + schema
                    + ".ini_predioinsumos ADD CONSTRAINT st_unique_gc UNIQUE (gc_predio_catastro)");
            stmt12.execute();

            PreparedStatement stmt13 = connection.prepareStatement("ALTER TABLE " + schema
                    + ".ini_predioinsumos ADD CONSTRAINT st_unique_snr UNIQUE (snr_predio_juridico)");
            stmt13.execute();

        } catch (Exception e) {
            log.error("Error protegiendo base de datos: " + e.getMessage());
            throw new BusinessException("No se ha podido configurar los permisos a la base de datos.");
        }

    }

    private void createExtensionsToDatabase(String database) {
        try {

            String url = "jdbc:postgresql://" + databaseHost + ":" + databasePort + "/" + database;

            Connection connection = DriverManager.getConnection(url, databaseUsername, databasePassword);

            PreparedStatement stmt1 = connection.prepareStatement("create extension	postgis");
            stmt1.execute();

            PreparedStatement stmt2 = connection.prepareStatement("create extension \"uuid-ossp\"");
            stmt2.execute();

        } catch (Exception e) {
            log.error("Error creando extensiones a la base de datos: " + e.getMessage());
        }
    }

    public void dropDatabase(String database, String username) throws BusinessException {

        try {

            String url = "jdbc:postgresql://" + databaseHost + ":" + databasePort + "/postgres";

            Connection connection = DriverManager.getConnection(url, databaseUsername, databasePassword);

            PreparedStatement stmt1 = connection
                    .prepareStatement("SELECT * FROM pg_stat_activity WHERE datname = '" + database + "';");
            stmt1.execute();

            PreparedStatement stmt2 = connection.prepareStatement(
                    "SELECT pg_terminate_backend (pg_stat_activity.pid) FROM pg_stat_activity WHERE pg_stat_activity.datname = '"
                            + database + "';");
            stmt2.execute();

            PreparedStatement stmt3 = connection.prepareStatement("drop database " + database);
            stmt3.execute();

            PreparedStatement stmt4 = connection.prepareStatement("drop user " + username);
            stmt4.execute();

        } catch (Exception e) {
            log.error("Error eliminando base de datos: " + e.getMessage());
            throw new BusinessException("No se ha podido eliminar la base de datos");
        }

    }

    public void createViewIntegration(String host, String port, String database, String schema, String nameView)
            throws BusinessException {

        try {

            String url = "jdbc:postgresql://" + host + ":" + port + "/" + database;

            Connection connection = DriverManager.getConnection(url, databaseUsername, databasePassword);

            PreparedStatement stmt1 = connection.prepareStatement("CREATE OR REPLACE VIEW " + schema + "." + nameView
                    + " AS\n"
                    + "  select id, numero_predial, coalesce(c.tipo_emparejamiento,0) as emparejamiento , geometria  from\n"
                    + "(select * from\n" + "       (select p.t_id as id, p.numero_predial, t.geometria\n"
                    + "      from  " + schema + ".gc_prediocatastro as p\n" + "inner join " + schema
                    + ".gc_terreno as t\n" + "   on t.gc_predio=p.t_id) as a\n" + "  left join\n"
                    + "  (select l.t_id as id2, i.tipo_emparejamiento\n" + "      from " + schema
                    + ".ini_predioinsumos as i\n" + "         inner join " + schema + ".gc_prediocatastro as l\n"
                    + "   on l.t_id=i.gc_predio_catastro) as b\n" + " on a.id=b.id2\n" + ") as c;");
            stmt1.execute();

        } catch (Exception e) {
            log.error("Error creando vista para el proceso de integración: " + e.getMessage());
            throw new BusinessException("No se ha podido configurar la integración.");
        }

    }

}
