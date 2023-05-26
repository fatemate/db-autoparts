package ru.edu.nsu.fit.martynov.autoparts.utils;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;


public class TableUtils {
    public static void setTableFromSqlResult(TableView table, ResultSet sqlResult) {
        try {
            table.getColumns().clear();
            ResultSetTableModel model = new ResultSetTableModel(sqlResult);

            for (int i = 0; i < model.getColumnNames().size(); i++) {
                int index = i;
                TableColumn<ObservableList<SimpleObjectProperty<Object>>, Object> column
                        = new TableColumn<>(model.getColumnName(i).get());
                Class<?> columnType = model.getColumnType(i);
                // SELECT * FROM Users
                column.setCellValueFactory(cellData -> cellData.getValue().get(index));
                table.getColumns().add(column);
            }
            table.setItems(model.getData());
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public SimpleObjectProperty func(TableColumn.CellDataFeatures<ObservableList<SimpleObjectProperty<Object>>, Object> cellData) {
        Object value = cellData.getValue().get(1).get();
        if (value == null) {
            return new SimpleObjectProperty<>(null);
        } else {
            BigDecimal bigDecimalValue = (BigDecimal) value;
            return new SimpleObjectProperty<>(bigDecimalValue.doubleValue());
        }
    }

    public static class ResultSetTableModel {
        private ObservableList<ObservableList<SimpleObjectProperty<Object>>> data;
        private ObservableList<SimpleStringProperty> columnNames;
        private ObservableList<Class<?>> columnTypes;

        public ResultSetTableModel(ResultSet rs) throws SQLException, ClassNotFoundException {
            data = FXCollections.observableArrayList();
            columnNames = FXCollections.observableArrayList();
            columnTypes = FXCollections.observableArrayList();

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                columnNames.add(new SimpleStringProperty(columnName));
                Class<?> columnType = Class.forName(metaData.getColumnClassName(i));
                columnTypes.add(columnType);
            }

            while (rs.next()) {
                ObservableList<SimpleObjectProperty<Object>> row = FXCollections.observableArrayList();
                for (int i = 1; i <= columnCount; i++) {
                    Object value = rs.getObject(i);
                    SimpleObjectProperty<Object> cell = new SimpleObjectProperty<>(value);
                    row.add(cell);
                }
                data.add(row);
            }
        }

        public ObservableList<ObservableList<SimpleObjectProperty<Object>>> getData() {
            return FXCollections.observableList(data);
        }

        public ObservableList<SimpleObjectProperty<Object>> getData(int i) {
            return data.get(i);
        }

        public ObservableList<SimpleStringProperty> getColumnNames() {
            return FXCollections.observableList(columnNames);
        }

        public SimpleStringProperty getColumnName(int i) {
            return columnNames.get(i);
        }

        public ObservableList<Class<?>> getColumnTypes() {
            return FXCollections.observableList(columnTypes);
        }

        public Class<?> getColumnType(int i) {
            return columnTypes.get(i);
        }
    }
}
