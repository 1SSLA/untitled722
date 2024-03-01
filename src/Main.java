java.util.ArrayList;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

class City {
    private String name;
    private List<Building> buildings;

    public City() {
        buildings = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addBuilding(Building building) {
        buildings.add(building);
    }

    public void removeBuilding(Building building) {
        buildings.remove(building);
    }

    public List<Building> getBuildings() {
        return buildings;
    }

    // Method to search for a building by street name and house number
    public Building findBuilding(String streetName, String houseNumber) {
        for (Building building : buildings) {
            if (building.getStreetName().equalsIgnoreCase(streetName) && building.getHouseNumber().equalsIgnoreCase(houseNumber)) {
                return building;
            }
        }
        return null; // If no matching building found
    }

    // Method to search for buildings containing a certain room
    public List<Building> findBuildingsByRoom(String roomNumber) {
        List<Building> matchingBuildings = new ArrayList<>();
        for (Building building : buildings) {
            for (Room room : building.getRooms()) {
                if (room.getNumber().equalsIgnoreCase(roomNumber)) {
                    matchingBuildings.add(building);
                    break;
                }
            }
        }
        return matchingBuildings;
    }
}

class Building {
    private String streetName;
    private String houseNumber;
    private double basicMonthlyPaymentPerSqM;
    private List<Room> rooms;

    public Building() {
        rooms = new ArrayList<>();
    }

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
    }

    public double getBasicMonthlyPaymentPerSqM() {
        return basicMonthlyPaymentPerSqM;
    }

    public void setBasicMonthlyPaymentPerSqM(double basicMonthlyPaymentPerSqM) {
        this.basicMonthlyPaymentPerSqM = basicMonthlyPaymentPerSqM;
    }

    public void addRoom(Room room) {
        room.setBuilding(this); // Set the building reference for the room
        rooms.add(room);
    }

    public void removeRoom(Room room) {
        rooms.remove(room);
    }

    public List<Room> getRooms() {
        return rooms;
    }

    // Method to calculate total area of all rooms in the building
    public double getTotalArea() {
        double totalArea = 0;
        for (Room room : rooms) {
            totalArea += room.getArea();
        }
        return totalArea;
    }
}

class Room {
    private String number;
    private double area;
    private Building building; // Reference to the building the room belongs to

    public Room(String number) {
        this.number = number;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public double getArea() {
        return area;
    }

    public void setArea(double area) {
        this.area = area;
    }

    public Building getBuilding() {
        return building;
    }

    public void setBuilding(Building building) {
        this.building = building;
    }
}

public class Main extends JFrame {
    private static final String URL = "jdbc:mysql://localhost:3306/city";
    private static final String USER = "root";
    private static final String PASSWORD = "password";

    private JTextField streetField, houseField, paymentField, roomNumberField, roomAreaField;
    private JButton addButton, updateButton, deleteButton, showButton, searchButton;
    private JTextArea outputArea;
    private City city;

    private JFrame roomManagementFrame;
    private JTable roomTable;
    private DefaultTableModel roomTableModel;
    private JButton addRoomButton, updateRoomButton, deleteRoomButton, closeRoomManagementButton;

    public Main() {
        city = new City();
        city.setName("Astana");

        setTitle("Building Database App");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(6, 2));
        inputPanel.add(new JLabel("Street Name:"));
        streetField = new JTextField();
        inputPanel.add(streetField);
        inputPanel.add(new JLabel("House Number:"));
        houseField = new JTextField();
        inputPanel.add(houseField);
        inputPanel.add(new JLabel("Basic Monthly Payment Per SqM:"));
        paymentField = new JTextField();
        inputPanel.add(paymentField);
        inputPanel.add(new JLabel("Room Number:"));
        roomNumberField = new JTextField();
        inputPanel.add(roomNumberField);
        inputPanel.add(new JLabel("Room Area:"));
        roomAreaField = new JTextField();
        inputPanel.add(roomAreaField);
        addButton = new JButton("Add Building");
        inputPanel.add(addButton);
        updateButton = new JButton("Update Building");
        inputPanel.add(updateButton);
        deleteButton = new JButton("Delete Building");
        inputPanel.add(deleteButton);
        searchButton = new JButton("Search Building by Room");
        inputPanel.add(searchButton);
        showButton = new JButton("Show Buildings");
        inputPanel.add(showButton);

        add(inputPanel, BorderLayout.NORTH);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);
        add(scrollPane, BorderLayout.CENTER);

        addButton.addActionListener(e -> addBuilding());
        updateButton.addActionListener(e -> updateBuilding());
        deleteButton.addActionListener(e -> deleteBuilding());
        searchButton.addActionListener(e -> searchBuildingByRoom());
        showButton.addActionListener(e -> displayBuildings());

        setVisible(true);
    }



    private void addBuilding() {
        String streetName = streetField.getText();
        String houseNumber = houseField.getText();
        double basicPayment = Double.parseDouble(paymentField.getText());

        // Создаем новое здание
        Building building = new Building();
        building.setStreetName(streetName);
        building.setHouseNumber(houseNumber);
        building.setBasicMonthlyPaymentPerSqM(basicPayment);

        // Получаем информацию о комнате из текстовых полей
        String roomNumber = roomNumberField.getText();
        double roomArea = Double.parseDouble(roomAreaField.getText());

        // Создаем объект Room с указанным номером комнаты
        Room room = new Room(roomNumber);
        room.setArea(roomArea);

        // Добавляем комнату в здание
        building.addRoom(room);

        // Обновляем список зданий в городе
        city.addBuilding(building);

        // Сохраняем информацию о здании и комнате в базе данных
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            // Вставляем информацию о здании в таблицу buildings
            String insertBuildingSQL = "INSERT INTO buildings (street_name, house_number, basic_monthly_payment_per_sqm) VALUES (?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(insertBuildingSQL)) {
                statement.setString(1, streetName);
                statement.setString(2, houseNumber);
                statement.setDouble(3, basicPayment);
                statement.executeUpdate();
            }

            // Вставляем информацию о комнате в таблицу rooms
            String insertRoomSQL = "INSERT INTO rooms (building_street_name, building_house_number, room_number, area) VALUES (?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(insertRoomSQL)) {
                statement.setString(1, streetName);
                statement.setString(2, houseNumber);
                statement.setString(3, roomNumber);
                statement.setDouble(4, roomArea);
                statement.executeUpdate();
            }

            outputArea.append("New building added: " + streetName + " " + houseNumber + "\n");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }


    private void updateBuilding() {
        String streetName = streetField.getText();
        String houseNumber = houseField.getText();
        double basicPayment = Double.parseDouble(paymentField.getText());
        Building selectedBuilding = city.findBuilding(streetName, houseNumber);
        if (selectedBuilding != null) {
            selectedBuilding.setBasicMonthlyPaymentPerSqM(basicPayment);
            try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
                 PreparedStatement statement = connection.prepareStatement("UPDATE buildings SET basic_monthly_payment_per_sqm = ? WHERE street_name = ? AND house_number = ?")) {
                statement.setDouble(1, basicPayment);
                statement.setString(2, streetName);
                statement.setString(3, houseNumber);
                int rowsUpdated = statement.executeUpdate();
                if (rowsUpdated > 0) {
                    outputArea.append("Building updated: " + streetName + " " + houseNumber + "\n");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } else {
            outputArea.append("Building not found: " + streetName + " " + houseNumber + "\n");
        }
    }

    private void deleteBuilding() {
        String streetName = streetField.getText();
        String houseNumber = houseField.getText();
        Building selectedBuilding = city.findBuilding(streetName, houseNumber);
        if (selectedBuilding != null) {
            try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
                // Удалить комнаты, связанные с этим зданием
                String deleteRoomsSQL = "DELETE FROM rooms WHERE building_street_name = ? AND building_house_number = ?";
                try (PreparedStatement deleteRoomsStatement = connection.prepareStatement(deleteRoomsSQL)) {
                    deleteRoomsStatement.setString(1, streetName);
                    deleteRoomsStatement.setString(2, houseNumber);
                    deleteRoomsStatement.executeUpdate();
                }

                // Удалить здание из таблицы buildings
                String deleteBuildingSQL = "DELETE FROM buildings WHERE street_name = ? AND house_number = ?";
                try (PreparedStatement deleteBuildingStatement = connection.prepareStatement(deleteBuildingSQL)) {
                    deleteBuildingStatement.setString(1, streetName);
                    deleteBuildingStatement.setString(2, houseNumber);
                    int rowsDeleted = deleteBuildingStatement.executeUpdate();
                    if (rowsDeleted > 0) {
                        outputArea.append("Building deleted: " + streetName + " " + houseNumber + "\n");
                        city.removeBuilding(selectedBuilding); // Удалить здание из списка города
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } else {
            outputArea.append("Building not found: " + streetName + " " + houseNumber + "\n");
        }
    }



    private void displayBuildings() {
        outputArea.setText("");
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM buildings")) {
            while (resultSet.next()) {
                String streetName = resultSet.getString("street_name");
                String houseNumber = resultSet.getString("house_number");
                double basicPayment = resultSet.getDouble("basic_monthly_payment_per_sqm");
                outputArea.append("Street Name: " + streetName + ", House Number: " + houseNumber + ", Basic Payment: " + basicPayment + "\n");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void searchBuildingByRoom() {
        String roomNumber = roomNumberField.getText();
        outputArea.setText("");
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement("SELECT b.street_name, b.house_number FROM buildings b JOIN rooms r ON b.street_name = r.building_street_name AND b.house_number = r.building_house_number WHERE r.room_number = ?")) {
            statement.setString(1, roomNumber);
            ResultSet resultSet = statement.executeQuery();

            if (!resultSet.isBeforeFirst()) {
                outputArea.append("No buildings found with room number: " + roomNumber + "\n");
            } else {
                outputArea.append("Buildings found with room number " + roomNumber + ":\n");
                while (resultSet.next()) {
                    String streetName = resultSet.getString("street_name");
                    String houseNumber = resultSet.getString("house_number");
                    outputArea.append("Street Name: " + streetName + ", House Number: " + houseNumber + "\n");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            outputArea.append("Error occurred while searching buildings by room number.\n");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }
}
