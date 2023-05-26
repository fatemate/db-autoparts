package ru.edu.nsu.fit.martynov.autoparts;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.AnchorPane;
import ru.edu.nsu.fit.martynov.autoparts.db.ChoiceHelper;
import ru.edu.nsu.fit.martynov.autoparts.db.DataBaseController;
import ru.edu.nsu.fit.martynov.autoparts.db.Queries;
import ru.edu.nsu.fit.martynov.autoparts.db.Query;
import ru.edu.nsu.fit.martynov.autoparts.utils.TableUtils;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StoreController {
    private Clipboard clipboard = Clipboard.getSystemClipboard();

    protected void init() {
        queriesPageQuerieChoice.setItems(Queries.getInstance().getQueries());

        try {
            loginPageImage.setImage(new Image(StoreApplication.class.getResource("favicon.png").openStream()));
            ((ImageView) sqlPageCopyButton.getGraphic())
                    .setImage(new Image(StoreApplication.class.getResource("icon-copy.png").openStream()));
            ((ImageView) sqlPageClearButton.getGraphic())
                    .setImage(new Image(StoreApplication.class.getResource("icon-clear.png").openStream()));
            ((ImageView) sqlPageSelectAllButton.getGraphic())
                    .setImage(new Image(StoreApplication.class.getResource("icon-select.png").openStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Login page
    @FXML
    private AnchorPane loginPage;

    @FXML private TextField loginPageLoginField;
    @FXML private PasswordField loginPagePasswordField;
    @FXML private Label loginPageErrorMessage;
    @FXML private ImageView loginPageImage;
    @FXML private Button loginPageSignInButton;

    @FXML
    protected void setLoginPageErrorMessage(String message) {
        loginPageErrorMessage.setText(message);
    }

    @FXML
    protected void signIn() {
        try {
            if (AuthorizationManager.authorize(loginPageLoginField.getText().trim(), loginPagePasswordField.getText())) {
                loginPageLoginField.clear();
                loginPagePasswordField.clear();
                loginPageErrorMessage.setVisible(false);
                loginPage.setVisible(false);
                authorizedPage.setVisible(true);

            } else {
                incorrectPasswordLoginPage();
            }
        } catch (SQLException e) {
            sqlConnectionRefused();
        }
    }

    protected void incorrectPasswordLoginPage() {
        loginPageErrorMessage.setText("Incorrect login or password. Try again!");
        loginPagePasswordField.clear();
        loginPagePasswordField.requestFocus();
        loginPageErrorMessage.setVisible(true);
    }

    protected void sqlConnectionLost() {
        authorizedPage.setVisible(false);
        loginPage.setVisible(true);
        loginPageErrorMessage.setText("Database connection lost");
        loginPageErrorMessage.setVisible(true);
    }

    protected void sqlConnectionRefused() {
        loginPageErrorMessage.setText("Database connection refused");
        loginPageErrorMessage.setVisible(true);
    }

    // Authorized page
    @FXML
    TabPane authorizedPage;

    // InsertPage
    // QueriesPage
    @FXML ComboBox<Query> queriesPageQuerieChoice;
    @FXML TableView queriesPageTable;
    @FXML Label queriesPageMessageField;
    @FXML Label queriesPageLabel1;
    @FXML Label queriesPageLabel2;
    @FXML Label queriesPageLabel3;
    @FXML Label queriesPageLabel4;
    @FXML Label queriesPageLabel5;
    @FXML Label queriesPageLabel6;
    @FXML Label queriesPageLabel7;
    @FXML Label queriesPageLabel8;
    @FXML ComboBox queriesPageChoice1;
    @FXML ComboBox queriesPageChoice2;
    @FXML ComboBox queriesPageChoice3;
    @FXML ComboBox queriesPageChoice4;
    @FXML ComboBox queriesPageChoice5;
    @FXML ComboBox queriesPageChoice6;
    @FXML ComboBox queriesPageChoice7;
    @FXML ComboBox queriesPageChoice8;
    @FXML DatePicker queriesPageDate1;
    @FXML DatePicker queriesPageDate2;
    @FXML DatePicker queriesPageDate3;
    @FXML DatePicker queriesPageDate4;
    @FXML DatePicker queriesPageDate5;
    @FXML DatePicker queriesPageDate6;
    @FXML DatePicker queriesPageDate7;
    @FXML DatePicker queriesPageDate8;

    @FXML
    protected void executeSqlQuery() {
        if (queriesPageQuerieChoice.getValue() == null) return;

    }

    @FXML
    protected void chooceSqlQuery() {
        if (queriesPageQuerieChoice.getValue() == null) return;
        clearQueryPage();
        switch (queriesPageQuerieChoice.getValue().toString()) {
            case "Запрос 4":
            case "Запрос 5.1":
            case "Запрос 5.2":
            case "Запрос 8":
            case "Запрос 13":
            case "Запрос 15.1":
            case "Запрос 15.2":
            case "Запрос 16.1":
            case "Запрос 16.2":
                break;

            case "Запрос 1.1":
            case "Запрос 1.2":
                queriesPageLabel1.setText("Тип поставщика:");
                queriesPageLabel1.setVisible(true);
                getValuesFromQuery("SELECT * FROM Supplier_types", queriesPageChoice1);
                initComboBox(queriesPageChoice1);
                queriesPageChoice1.setVisible(true);

                queriesPageLabel2.setText("Вид товара:");
                queriesPageLabel2.setVisible(true);
                queriesPageChoice2.setVisible(true);

                queriesPageLabel3.setText("Товар:");
                queriesPageLabel3.setVisible(true);
                queriesPageChoice3.setVisible(true);

                queriesPageLabel4.setText("Объём:");
                queriesPageLabel4.setVisible(true);
                Pattern pattern = Pattern.compile("\\d*");
                TextFormatter<String> formatter = new TextFormatter<>(change -> {
                    if (!pattern.matcher(change.getControlNewText()).matches()) {
                        return null;
                    }
                    return change;
                });
                queriesPageChoice4.getEditor().setTextFormatter(formatter);
                queriesPageChoice4.setVisible(true);

                queriesPageLabel5.setText("Дата начала:");
                queriesPageLabel5.setVisible(true);
                queriesPageDate5.setDayCellFactory(picker -> new DateCell() {
                    @Override
                    public void updateItem(LocalDate date, boolean empty) {
                        super.updateItem(date, empty);
                        if (queriesPageDate6.getValue() != null)
                            setDisable(date.isAfter(queriesPageDate6.getValue())); // Отключаем ячейки дней, которые меньше минимальной даты
                    }
                });
                queriesPageDate5.setVisible(true);

                queriesPageLabel6.setText("Дата конца:");
                queriesPageLabel6.setVisible(true);
                queriesPageDate6.setDayCellFactory(picker -> new DateCell() {
                    @Override
                    public void updateItem(LocalDate date, boolean empty) {
                        super.updateItem(date, empty);
                        if (queriesPageDate5.getValue() != null)
                            setDisable(date.isBefore(queriesPageDate5.getValue())); // Отключаем ячейки дней, которые меньше минимальной даты
                    }
                });
                queriesPageDate6.setVisible(true);
                //queriesPageChoice1

            case "Запрос 2":

            case "Запрос 3.1":
            case "Запрос 3.2":

            case "Запрос 6":

            case "Запрос 7.1":

            case "Запрос 7.2":

            case "Запрос 9.1":
            case "Запрос 9.2":

            case "Запрос 10":

            case "Запрос 11":

            case "Запрос 12":

            case "Запрос 14":

            default:
        }
    }

    protected void clearQueryPage() {
        queriesPageTable.setVisible(false);
        queriesPageMessageField.setVisible(false);
        queriesPageLabel1.setVisible(false);
        queriesPageLabel2.setVisible(false);
        queriesPageLabel3.setVisible(false);
        queriesPageLabel4.setVisible(false);
        queriesPageLabel5.setVisible(false);
        queriesPageLabel6.setVisible(false);
        queriesPageLabel7.setVisible(false);
        queriesPageLabel8.setVisible(false);
        queriesPageChoice1.setVisible(false);
        queriesPageChoice2.setVisible(false);
        queriesPageChoice3.setVisible(false);
        queriesPageChoice4.setVisible(false);
        queriesPageChoice5.setVisible(false);
        queriesPageChoice6.setVisible(false);
        queriesPageChoice7.setVisible(false);
        queriesPageChoice8.setVisible(false);
        queriesPageDate1.setVisible(false);
        queriesPageDate2.setVisible(false);
        queriesPageDate3.setVisible(false);
        queriesPageDate4.setVisible(false);
        queriesPageDate5.setVisible(false);
        queriesPageDate6.setVisible(false);
        queriesPageDate7.setVisible(false);
        queriesPageDate8.setVisible(false);

        queriesPageTable.getColumns().clear();
        queriesPageMessageField.setText("");
        queriesPageLabel1.setText("");;
        queriesPageLabel2.setText("");;
        queriesPageLabel3.setText("");;
        queriesPageLabel4.setText("");;
        queriesPageLabel5.setText("");;
        queriesPageLabel6.setText("");;
        queriesPageLabel7.setText("");;
        queriesPageLabel8.setText("");;
        queriesPageChoice1.setItems(null);
        queriesPageChoice2.setItems(null);
        queriesPageChoice3.setItems(null);
        queriesPageChoice4.setItems(null);
        queriesPageChoice5.setItems(null);
        queriesPageChoice6.setItems(null);
        queriesPageChoice7.setItems(null);
        queriesPageChoice8.setItems(null);
        queriesPageDate1.setValue(null);
        queriesPageDate2.setValue(null);
        queriesPageDate3.setValue(null);
        queriesPageDate4.setValue(null);
        queriesPageDate5.setValue(null);
        queriesPageDate6.setValue(null);
        queriesPageDate7.setValue(null);
        queriesPageDate8.setValue(null);
    }

    @FXML
    protected void comboBoxAllowOnlyExisting(ActionEvent event) {
        ComboBox comboBox = (ComboBox)event.getSource();
        String input = comboBox.getEditor().getText();
        if (comboBox.getItems().stream().filter(element -> element.toString().equals(input)).toList().isEmpty()) {
            comboBox.getSelectionModel().clearSelection();
            comboBox.setValue(null);
        }
    }

    // SQLPage
    @FXML TextArea sqlPageCommand;
    @FXML Button sqlPageExecuteButton;
    @FXML Button sqlPageClearButton;
    @FXML Button sqlPageCopyButton;
    @FXML Button sqlPageSelectAllButton;
    @FXML Label sqlPageMessageField;
    @FXML ProgressIndicator sqlPageProgressIndicator;
    @FXML TableView<ObservableList<SimpleObjectProperty<Object>>> sqlPageTable;

    @FXML
    protected void selectAllSqlCommand() {
        sqlPageCommand.selectAll();
        sqlPageCommand.requestFocus();
    }

    @FXML
    protected void clearSqlCommandField() {
        sqlPageCommand.clear();
        sqlPageCommand.requestFocus();
    }

    @FXML
    protected void copySqlCommandField() {
        ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(sqlPageCommand.getText());
        clipboard.setContent(clipboardContent);
        sqlPageCommand.requestFocus();
    }

    @FXML
    protected void executeSqlCommand() {
        sqlPageTable.setVisible(false);
        sqlPageMessageField.setVisible(false);

        Task<Object> task = new Task<Object>() {
            @Override
            protected Object call() throws Exception {
                Object sqlResult = null;
                if (!sqlPageCommand.getSelectedText().isEmpty())
                    sqlResult = DataBaseController.getInstance().executeStatement(sqlPageCommand.getSelectedText());
                else
                    sqlResult = DataBaseController.getInstance().executeStatement(sqlPageCommand.getText());
                return sqlResult;
            }
        };

        task.setOnSucceeded(event -> {
            Object result = task.getValue();
            sqlPageProgressIndicator.setVisible(false);
            if (sqlPageMessageField.isVisible() || sqlPageTable.isVisible()) return;

            if (result instanceof String) {
                sqlPageMessageField.setText((String) result);
                sqlPageMessageField.setVisible(true);
            }
            else {
                if (result instanceof ResultSet) {
                    TableUtils.setTableFromSqlResult(sqlPageTable, (ResultSet) result);
                    sqlPageTable.setVisible(true);
                }
            }
        });

        task.setOnFailed(event -> {
            Throwable exception = task.getException();
            sqlPageProgressIndicator.setVisible(false);

            if (exception instanceof SQLException) {
                sqlConnectionLost();
            }
        });

        new Thread(task).start();
        sqlPageProgressIndicator.setVisible(true);
    }

    private void initComboBox(ComboBox comboBox) {
        comboBox.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (!comboBox.isShowing()) {
                comboBox.show();
            }
            comboBox.getItems().setAll(comboBox.getItems().stream()
                    .filter(item -> ((ChoiceHelper)item).getName().toLowerCase().startsWith(newValue.toLowerCase()))
                    .collect(Collectors.toList()));
        });
    }

    private void getValuesFromQuery(String sql, ComboBox box) {
        ObservableList<ChoiceHelper> list = FXCollections.observableArrayList();
        box.setItems(null);

        Task<Object> task = new Task<Object>() {
            @Override
            protected Object call() throws Exception {
                Object sqlResult = DataBaseController.getInstance().executeStatement(sql);
                return sqlResult;
            }
        };

        task.setOnSucceeded(event -> {
            Object result = task.getValue();

            if (box.getItems() != null) return;

            if (result instanceof ResultSet) {
                ResultSet resultSet = (ResultSet) result;

                try {
                    while (resultSet.next()) {
                        list.add(new ChoiceHelper(resultSet.getInt(1), resultSet.getString(2)));
                    }
                } catch (SQLException e) {}

                box.setItems(list);
            }
        });

        task.setOnFailed(event -> {
            Throwable exception = task.getException();

            if (exception instanceof SQLException) {
                sqlConnectionLost();
            }
        });

        new Thread(task).start();
    }
}