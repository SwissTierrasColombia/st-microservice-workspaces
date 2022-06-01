package com.ai.st.microservice.workspaces.business;

import com.ai.st.microservice.common.exceptions.BusinessException;

import com.ai.st.microservice.workspaces.services.tracing.SCMTracing;
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
            String messageError = String.format("Error creando base de datos %s: %s", database, e.getMessage());
            SCMTracing.sendError(messageError);
            log.error(messageError);
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
            String messageError = String.format("Error protegiendo la base de datos %s: %s", database, e.getMessage());
            SCMTracing.sendError(messageError);
            log.error(messageError);
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
            String messageError = String.format("Error creando las extensiones en la base de datos %s: %s", database,
                    e.getMessage());
            SCMTracing.sendError(messageError);
            log.error(messageError);
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
            String messageError = String.format("Error eliminando la base de datos %s: %s", database, e.getMessage());
            SCMTracing.sendError(messageError);
            log.error(messageError);
            throw new BusinessException("No se ha podido eliminar la base de datos");
        }

    }

    public void createParcelIntegratedView(String host, String port, String database, String schema, String viewName)
            throws BusinessException {

        try {

            String url = "jdbc:postgresql://" + host + ":" + port + "/" + database;

            Connection connection = DriverManager.getConnection(url, databaseUsername, databasePassword);

            PreparedStatement stmt1 = connection.prepareStatement("CREATE OR REPLACE VIEW " + schema + "." + viewName
                    + " AS SELECT pc.t_id AS id,\n" + "    pc.numero_predial,\n" + "        CASE\n"
                    + "            WHEN pc.nupre IS NULL THEN 'NA'::character varying\n" + "            ELSE pc.nupre\n"
                    + "        END AS nupre,\n" + "    pc.circulo_registral,\n"
                    + "    pc.matricula_inmobiliaria_catastro,\n" + "        CASE\n"
                    + "            WHEN r.valor IS NULL THEN 'NA'::character varying\n" + "            ELSE r.valor\n"
                    + "        END AS direccion,\n" + "    pc.tipo_predio,\n"
                    + "    cpt.dispname AS condicion_predio,\n" + "        CASE\n"
                    + "            WHEN ep.estado_alerta IS NULL THEN 'NA'::character varying\n"
                    + "            ELSE ep.estado_alerta\n" + "        END AS estado_predio,\n" + "    t.geometria,\n"
                    + "    t.area_terreno_alfanumerica,\n" + "    t.area_terreno_digital,\n" + "        CASE\n"
                    + "            WHEN ipi.t_id IS NULL THEN false\n" + "            ELSE true\n"
                    + "        END AS cruzo\n" + "   FROM " + schema + ".gc_prediocatastro pc\n" + "     JOIN " + schema
                    + ".gc_terreno t ON t.gc_predio = pc.t_id AND t.geometria IS NOT NULL\n" + "     LEFT JOIN "
                    + schema + ".ini_predioinsumos ipi ON ipi.gc_predio_catastro = pc.t_id\n" + "     LEFT JOIN "
                    + schema + ".gc_condicionprediotipo cpt ON cpt.t_id = pc.condicion_predio\n" + "     LEFT JOIN "
                    + schema + ".gc_estadopredio ep ON ep.gc_prediocatastro_estado_predio = pc.t_id\n"
                    + "     LEFT JOIN LATERAL ( SELECT r_1.t_id,\n" + "            r_1.t_seq,\n"
                    + "            r_1.valor,\n" + "            r_1.principal,\n"
                    + "            r_1.geometria_referencia,\n" + "            r_1.gc_prediocatastro_direcciones\n"
                    + "           FROM " + schema + ".gc_direccion r_1\n"
                    + "          WHERE pc.t_id = r_1.gc_prediocatastro_direcciones\n" + "         LIMIT 1) r ON true\n"
                    + "  ORDER BY pc.t_id;");
            stmt1.execute();

        } catch (Exception e) {
            String messageError = String.format(
                    "Error creando la vista de predios integrados en la base de datos %s: %s", database,
                    e.getMessage());
            SCMTracing.sendError(messageError);
            log.error(messageError);
            throw new BusinessException("No se ha podido configurar la vista de predios integrados.");
        }

    }

    public void createPerimeterView(String host, String port, String database, String schema, String viewName)
            throws BusinessException {

        try {

            String url = "jdbc:postgresql://" + host + ":" + port + "/" + database;

            Connection connection = DriverManager.getConnection(url, databaseUsername, databasePassword);

            PreparedStatement stmt1 = connection.prepareStatement("CREATE OR REPLACE VIEW " + schema + "." + viewName
                    + " AS SELECT\n" + "        CASE\n"
                    + "            WHEN p.codigo_departamento IS NULL THEN 'NA'::character varying\n"
                    + "            ELSE p.codigo_departamento\n" + "        END AS codigo_departamento,\n"
                    + "        CASE\n" + "            WHEN p.codigo_municipio IS NULL THEN 'NA'::character varying\n"
                    + "            ELSE p.codigo_municipio\n" + "        END AS codigo_municipio,\n" + "        CASE\n"
                    + "            WHEN p.tipo_avaluo IS NULL THEN 'NA'::character varying\n"
                    + "            ELSE p.tipo_avaluo\n" + "        END AS tipo_avaluo,\n" + "        CASE\n"
                    + "            WHEN p.nombre_geografico IS NULL THEN 'NA'::character varying\n"
                    + "            ELSE p.nombre_geografico\n" + "        END AS nombre_geografico,\n"
                    + "        CASE\n" + "            WHEN p.codigo_nombre IS NULL THEN 'NA'::character varying\n"
                    + "            ELSE p.codigo_nombre\n" + "        END AS codigo_nombre,\n" + "    p.geometria\n"
                    + "   FROM " + schema + ".gc_perimetro p\n" + "  WHERE p.geometria IS NOT NULL;");
            stmt1.execute();

        } catch (Exception e) {
            String messageError = String.format("Error creando la vista de perímetros en la base de datos %s: %s",
                    database, e.getMessage());
            SCMTracing.sendError(messageError);
            log.error(messageError);
            throw new BusinessException("No se ha podido crear la vista de perímetros.");
        }

    }

    public void createSidewalkView(String host, String port, String database, String schema, String viewName)
            throws BusinessException {

        try {

            String url = "jdbc:postgresql://" + host + ":" + port + "/" + database;

            Connection connection = DriverManager.getConnection(url, databaseUsername, databasePassword);

            PreparedStatement stmt1 = connection.prepareStatement("CREATE OR REPLACE VIEW " + schema + "." + viewName
                    + " AS SELECT\n" + "        CASE\n"
                    + "            WHEN v.codigo IS NULL THEN 'NA'::character varying\n" + "            ELSE v.codigo\n"
                    + "        END AS codigo,\n" + "        CASE\n"
                    + "            WHEN v.codigo_anterior IS NULL THEN 'NA'::character varying\n"
                    + "            ELSE v.codigo_anterior\n" + "        END AS codigo_anterior,\n" + "        CASE\n"
                    + "            WHEN v.nombre IS NULL THEN 'NA'::character varying\n" + "            ELSE v.nombre\n"
                    + "        END AS nombre,\n" + "        CASE\n"
                    + "            WHEN v.codigo_sector IS NULL THEN 'NA'::character varying\n"
                    + "            ELSE v.codigo_sector\n" + "        END AS codigo_sector,\n" + "    v.geometria\n"
                    + "   FROM " + schema + ".gc_vereda v\n" + "  WHERE v.geometria IS NOT NULL;");
            stmt1.execute();

        } catch (Exception e) {
            String messageError = String.format("Error creando la vista de veredas en la base de datos %s: %s",
                    database, e.getMessage());
            SCMTracing.sendError(messageError);
            log.error(messageError);
            throw new BusinessException("No se ha podido crear la vista de veredas.");
        }

    }

    public void createBuildingView(String host, String port, String database, String schema, String viewName)
            throws BusinessException {

        try {

            String url = "jdbc:postgresql://" + host + ":" + port + "/" + database;

            Connection connection = DriverManager.getConnection(url, databaseUsername, databasePassword);

            PreparedStatement stmt1 = connection.prepareStatement("CREATE OR REPLACE VIEW " + schema + "." + viewName
                    + " AS SELECT c.t_id AS id,\n" + "    c.gc_predio AS id_predio,\n" + "        CASE\n"
                    + "            WHEN c.etiqueta IS NULL THEN 'NA'::character varying\n"
                    + "            ELSE c.etiqueta\n" + "        END AS etiqueta,\n"
                    + "    uct.dispname AS tipo_construccion,\n" + "    c.tipo_dominio,\n" + "    c.area_construida,\n"
                    + "    c.geometria\n" + "   FROM " + schema + ".gc_construccion c\n" + "     LEFT JOIN " + schema
                    + ".gc_unidadconstrucciontipo uct ON uct.t_id = c.tipo_construccion\n"
                    + "  WHERE c.geometria IS NOT NULL;");
            stmt1.execute();

        } catch (Exception e) {
            String messageError = String.format("Error creando la vista de construcciones en la base de datos %s: %s",
                    database, e.getMessage());
            SCMTracing.sendError(messageError);
            log.error(messageError);
            throw new BusinessException("No se ha podido crear la vista de construcciones.");
        }

    }

    public void createBuildingUnitsView(String host, String port, String database, String schema, String viewName)
            throws BusinessException {

        try {

            String url = "jdbc:postgresql://" + host + ":" + port + "/" + database;

            Connection connection = DriverManager.getConnection(url, databaseUsername, databasePassword);

            PreparedStatement stmt1 = connection.prepareStatement(
                    "CREATE OR REPLACE VIEW " + schema + "." + viewName + " AS SELECT uc.t_id AS id,\n"
                            + "        CASE\n" + "            WHEN uc.etiqueta IS NULL THEN 'NA'::character varying\n"
                            + "            ELSE uc.etiqueta\n" + "        END AS etiqueta,\n" + "        CASE\n"
                            + "            WHEN uc.tipo_dominio IS NULL THEN 'NA'::character varying\n"
                            + "            ELSE uc.tipo_dominio\n" + "        END AS tipo_dominio,\n"
                            + "    uct.dispname AS tipo_construccion,\n" + "    uc.total_habitaciones,\n"
                            + "    uc.total_banios,\n" + "    uc.total_pisos,\n" + "    uc.geometria\n" + "   FROM "
                            + schema + ".gc_unidadconstruccion uc\n" + "     LEFT JOIN " + schema
                            + ".gc_unidadconstrucciontipo uct ON uct.t_id = uc.tipo_construccion\n"
                            + "  WHERE uc.geometria IS NOT NULL;");
            stmt1.execute();

        } catch (Exception e) {
            String messageError = String.format(
                    "Error creando la vista de unidades de construcción en la base de datos %s: %s", database,
                    e.getMessage());
            SCMTracing.sendError(messageError);
            log.error(messageError);
            throw new BusinessException("No se ha podido crear la vista de unidades de construcción.");
        }

    }

    public void createSquareView(String host, String port, String database, String schema, String viewName)
            throws BusinessException {

        try {

            String url = "jdbc:postgresql://" + host + ":" + port + "/" + database;

            Connection connection = DriverManager.getConnection(url, databaseUsername, databasePassword);

            PreparedStatement stmt1 = connection.prepareStatement("CREATE OR REPLACE VIEW " + schema + "." + viewName
                    + " AS SELECT\n" + "        CASE\n"
                    + "            WHEN m.codigo IS NULL THEN 'NA'::character varying\n" + "            ELSE m.codigo\n"
                    + "        END AS codigo,\n" + "        CASE\n"
                    + "            WHEN m.codigo_anterior IS NULL THEN 'NA'::character varying\n"
                    + "            ELSE m.codigo_anterior\n" + "        END AS codigo_anterior,\n" + "        CASE\n"
                    + "            WHEN m.codigo_barrio IS NULL THEN 'NA'::character varying\n"
                    + "            ELSE m.codigo_barrio\n" + "        END AS codigo_barrio,\n" + "    m.geometria\n"
                    + "   FROM " + schema + ".gc_manzana m\n" + "  WHERE m.geometria IS NOT NULL;");
            stmt1.execute();

        } catch (Exception e) {
            String messageError = String.format("Error creando la vista de manzanas en la base de datos %s: %s",
                    database, e.getMessage());
            SCMTracing.sendError(messageError);
            log.error(messageError);
            throw new BusinessException("No se ha podido crear la vista de manzanas.");
        }

    }

}
