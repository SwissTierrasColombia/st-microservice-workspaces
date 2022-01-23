package com.ai.st.microservice.workspaces.dto.geovisor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MicroserviceSetupMapDto implements Serializable {

    private static final long serialVersionUID = 2755366603547812603L;

    private String name_conn;
    private String store;
    private String workspace;
    private String dbname;
    private String host;
    private String port;
    private String user;
    private String password;
    private String schema;
    private List<Layer> layers;

    public MicroserviceSetupMapDto() {
        this.layers = new ArrayList<>();
    }

    public String getName_conn() {
        return name_conn;
    }

    public void setName_conn(String name_conn) {
        this.name_conn = name_conn;
    }

    public String getDbname() {
        return dbname;
    }

    public void setDbname(String dbname) {
        this.dbname = dbname;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public List<Layer> getLayers() {
        return layers;
    }

    public void setLayers(List<Layer> layers) {
        this.layers = layers;
    }

    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        this.store = store;
    }

    public String getWorkspace() {
        return workspace;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    public static class Layer {

        private String tablename;
        private String style;
        private String title;

        public Layer() {

        }

        public Layer(String tableName, String style, String title) {
            this.tablename = tableName;
            this.style = style;
            this.title = title;
        }

        public String getTablename() {
            return tablename;
        }

        public void setTablename(String tablename) {
            this.tablename = tablename;
        }

        public String getStyle() {
            return style;
        }

        public void setStyle(String style) {
            this.style = style;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }

}
