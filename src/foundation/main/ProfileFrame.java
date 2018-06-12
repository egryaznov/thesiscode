package foundation.main;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import foundation.entity.MaritalBond;
import foundation.entity.Person;
import org.jdatepicker.JDatePicker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

class ProfileFrame extends JFrame
{
    private static final int NO_ID = -1;
    private JTextField firstNameEdit;
    private JTextField lastNameEdit;
    private JTextField occupationEdit;
    private JTextField phoneEdit;
    private JTextField emailEdit;
    private JPanel panel1;
    private JButton cancelBtn;
    private JButton saveChangesBtn;
    private JCheckBox maleCheckBox;
    private JDatePicker datePicker;
    private JLabel dateOfMarriageLabel;
    private JLabel spouseNameLabel;
    private @Nullable Person selectedPerson;
    private @NotNull
    final GenealogyView view;

    ProfileFrame(final @NotNull GenealogyView view)
    {
        this.view = view;
        // Set frame properties
        setSize(362, 343);
        setContentPane(panel1);
        setTitle("Kindred: Personal Information");
        setLocationRelativeTo(null);
        setResizable(false);
        setAlwaysOnTop(true);
        // Register listeners
        cancelBtn.addActionListener(new CancelBtnListener());
        addWindowListener(new ProfileFrameWindowListener());
    }


    private void setSaveChangesBtnListener(final @NotNull ActionListener newListener)
    {
        // Firstly remove all action listeners from this button
        for (ActionListener al : saveChangesBtn.getActionListeners())
        {
            saveChangesBtn.removeActionListener(al);
        }
        // Then set the new listener
        saveChangesBtn.addActionListener(newListener);
    }

    /*
        Shows this frame in "Create New Profile" mode.
     */
    void createNewProfile(final double newNodeX, final double newNodeY)
    {
        setTitle("Kindred: Add a New Profile");
        saveChangesBtn.setText("Create Profile");
        this.selectedPerson = null;
        setSaveChangesBtnListener(new CreateProfileBtnListener(newNodeX, newNodeY));
        display();
    }

    /*
        Shows this frame in "Update Profile" mode.
     */
    void updateProfile(final @NotNull Person selectedPerson)
    {
        setTitle("Kindred: Edit Profile");
        saveChangesBtn.setText("Save Changes");
        this.selectedPerson = selectedPerson;
        setSaveChangesBtnListener(new UpdateProfileBtnListener(selectedPerson));
        // Load first and last name from the node
        firstNameEdit.setText(selectedPerson.getFirstName());
        lastNameEdit.setText(selectedPerson.getLastName());
        // Load sex from the node
        maleCheckBox.setSelected(selectedPerson.isMale());
        datePicker.getFormattedTextField().setValue(Person.stringToCalendar(selectedPerson.getDateOfBirth()));
        // Load other fields
        occupationEdit.setText(selectedPerson.getOccupation());
        phoneEdit.setText(selectedPerson.getPhone());
        emailEdit.setText(selectedPerson.getEmail());
        // Retrieving marriage information from model
        final @Nullable MaritalBond marriage = view.getModel().getMaritalBond(selectedPerson);
        if (marriage != null)
        {
            // Setting the first name, last name and the wedding date
            final @NotNull Person spouse = (marriage.getHead().equals(selectedPerson)) ? marriage.getTail() : marriage.getHead();
            spouseNameLabel.setText(spouse.getFirstName() + " " + spouse.getLastName());
            dateOfMarriageLabel.setText(marriage.getDateOfWedding());
        }
        // Show the form after the successful loading of information
        display();
    }

    /*
        Displays this frame.
     */
    private void display()
    {
        final @NotNull JFrame mainFrame = view.getParentFrame();
        mainFrame.setTitle(MainFrame.TITLE + " DISABLED");
        mainFrame.setEnabled(false);
        if (selectedPerson != null)
        {
            selectedPerson.select();
        }
        setVisible(true);
    }

    /*
        Hides this frame.
     */
    private void conceal()
    {
        // Clear model
        final @NotNull JFrame mainFrame = view.getParentFrame();
        // Clear all info fields
        firstNameEdit.setText("");
        lastNameEdit.setText("");
        maleCheckBox.setSelected(true);
        datePicker.getFormattedTextField().setText("");
        occupationEdit.setText("");
        phoneEdit.setText("");
        emailEdit.setText("");
        spouseNameLabel.setText("Not Married");
        dateOfMarriageLabel.setText("Not Married");
        // Enable main window
        mainFrame.setEnabled(true);
        mainFrame.setTitle(MainFrame.TITLE);
        if (selectedPerson != null)
        {
            selectedPerson.deselect();
        }
        selectedPerson = null;
        setVisible(false);
    }

    /*
        Removes all quotes from the string `value` and puts it in the `map` as a (field -> value) entry.
     */
    private void washAndPutString(final @NotNull Map<String, String> map, final String field, final String value)
    {
        final String washedValue = Database.instance.wash(value);
        map.put(field, washedValue);
    }

    /*
        Collects values from all text field and puts them into a map of scheme: DATABASE_ATTRIBUTE -> VALUE
     */
    private @NotNull Map<String, String> gatherInfo()
    {
        // Put a name
        final @NotNull Map<String, String> column = new HashMap<>();
        washAndPutString(column, Database.FIRST_NAME_COLUMN, firstNameEdit.getText());
        washAndPutString(column, Database.LAST_NAME_COLUMN, lastNameEdit.getText());
        // Put a date
        final @NotNull Calendar birthDate = (Calendar) datePicker.getFormattedTextField().getValue();
        column.put(Database.DATE_OF_BIRTH_COLUMN, Person.calendarToString(birthDate));
        // Put sex
        final String sex = (maleCheckBox.isSelected()) ? Database.MALE : Database.FEMALE;
        column.put(Database.SEX_COLUMN, sex);
        // Put other values
        washAndPutString(column, Database.OCCUPATION_COLUMN, occupationEdit.getText());
        washAndPutString(column, Database.PHONE_NUMBER_COLUMN, phoneEdit.getText());
        washAndPutString(column, Database.EMAIL_COLUMN, emailEdit.getText());
        // Return constructed map
        return column;
    }

    /*
        Checks if the user fills the profile form correctly.
     */
    private boolean profileInvalid()
    {
        final String firstName = Database.instance.wash(firstNameEdit.getText());
        final String lastName = Database.instance.wash(lastNameEdit.getText());
        return (firstName.isEmpty() || (lastName.isEmpty()));
    }

    /*
        Shows the error message to the user if he messed up filling the profile.
     */
    private void notifyProfileInvalid()
    {
        ProfileFrame.this.setAlwaysOnTop(false);
        JOptionPane.showMessageDialog(
                null,
                "One or more required fields are empty",
                "Hint",
                JOptionPane.INFORMATION_MESSAGE
        );
        ProfileFrame.this.setAlwaysOnTop(true);
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$()
    {
        panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(12, 5, new Insets(0, 0, 0, 0), -1, -1));
        final JLabel label1 = new JLabel();
        label1.setText("First Name*");
        panel1.add(label1, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        firstNameEdit = new JTextField();
        panel1.add(firstNameEdit, new GridConstraints(1, 2, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(133, 28), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Last Name*");
        panel1.add(label2, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Sex*");
        panel1.add(label3, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Date of Birth*");
        panel1.add(label4, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Birthplace:");
        panel1.add(label5, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        occupationEdit = new JTextField();
        panel1.add(occupationEdit, new GridConstraints(5, 2, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(133, 28), null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Phone Number:");
        panel1.add(label6, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        phoneEdit = new JTextField();
        panel1.add(phoneEdit, new GridConstraints(6, 2, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(133, 28), null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("E-mail:");
        panel1.add(label7, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        emailEdit = new JTextField();
        panel1.add(emailEdit, new GridConstraints(7, 2, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(133, 28), null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel1.add(spacer2, new GridConstraints(2, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        cancelBtn = new JButton();
        cancelBtn.setText("Cancel");
        panel1.add(cancelBtn, new GridConstraints(10, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        saveChangesBtn = new JButton();
        saveChangesBtn.setHideActionText(false);
        saveChangesBtn.setText("Save Changes");
        panel1.add(saveChangesBtn, new GridConstraints(10, 2, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(133, 27), null, 0, false));
        maleCheckBox = new JCheckBox();
        maleCheckBox.setText("Male");
        panel1.add(maleCheckBox, new GridConstraints(3, 2, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        lastNameEdit = new JTextField();
        panel1.add(lastNameEdit, new GridConstraints(2, 2, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(133, 28), null, 0, false));
        datePicker = new JDatePicker();
        datePicker.setShowYearButtons(true);
        datePicker.setTextEditable(false);
        panel1.add(datePicker, new GridConstraints(4, 2, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label8 = new JLabel();
        label8.setText("Married to:");
        panel1.add(label8, new GridConstraints(8, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label9 = new JLabel();
        label9.setText("Date of Marriage:");
        panel1.add(label9, new GridConstraints(9, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        dateOfMarriageLabel = new JLabel();
        dateOfMarriageLabel.setText("Not Married");
        panel1.add(dateOfMarriageLabel, new GridConstraints(9, 2, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        spouseNameLabel = new JLabel();
        spouseNameLabel.setText("Not Married");
        panel1.add(spouseNameLabel, new GridConstraints(8, 2, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        panel1.add(spacer3, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        panel1.add(spacer4, new GridConstraints(11, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$()
    {
        return panel1;
    }


    private class UpdateProfileBtnListener implements ActionListener
    {
        private final @NotNull Person personToUpdate;

        UpdateProfileBtnListener(final @NotNull Person personToUpdate)
        {
            this.personToUpdate = personToUpdate;
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            if (profileInvalid())
            {
                notifyProfileInvalid();
            }
            else
            {
                final @NotNull Map<String, String> row = gatherInfo();
                // Update the node itself
                personToUpdate.setFirstName(row.get(Database.FIRST_NAME_COLUMN));
                personToUpdate.setLastName(row.get(Database.LAST_NAME_COLUMN));
                personToUpdate.setSex(row.get(Database.SEX_COLUMN));
                personToUpdate.setDateOfBirth(row.get(Database.DATE_OF_BIRTH_COLUMN));
                personToUpdate.setOccupation(row.get(Database.OCCUPATION_COLUMN));
                personToUpdate.setPhone(row.get(Database.PHONE_NUMBER_COLUMN));
                personToUpdate.setEmail(row.get(Database.EMAIL_COLUMN));
                conceal();
            }
        }
    }

    private class CancelBtnListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            conceal();
        }
    }

    private class CreateProfileBtnListener implements ActionListener
    {
        private final double nodeX;
        private final double nodeY;

        CreateProfileBtnListener(final double nodeX, final double nodeY)
        {
            this.nodeX = nodeX;
            this.nodeY = nodeY;
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            if (profileInvalid())
            {
                notifyProfileInvalid();
            }
            else
            {
                final @NotNull Map<String, String> row = gatherInfo();
                // Insert the constructed record into the DB
                final int personID = Database.instance.insertPerson(row);
                if (personID != NO_ID)
                {
                    // Insert a photo for this person and attach it
                    final String path = Database.NO_PHOTO;
                    final int newPhotoID = Database.instance.insertPhoto(path, nodeX, nodeY);
                    final String insertPhoto = String.format("INSERT INTO has(person_id, photo_id) VALUES (%d, %d)",
                            personID, newPhotoID);
                    Database.instance.issueSQL(insertPhoto);
                    // Successfully inserted new person into DB
                    view.getModel().addPerson(new Person(
                            personID,
                            firstNameEdit.getText(),
                            lastNameEdit.getText(),
                            nodeX,
                            nodeY,
                            null,
                            maleCheckBox.isSelected(),
                            row.get(Database.DATE_OF_BIRTH_COLUMN),
                            row.get(Database.OCCUPATION_COLUMN),
                            row.get(Database.PHONE_NUMBER_COLUMN),
                            row.get(Database.EMAIL_COLUMN)
                    ));
                    // Hide the frame after everything was done
                    conceal();
                }
                else
                {
                    JOptionPane.showMessageDialog(null,
                            "Error inserting new person in DB",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        }
    }

    private class ProfileFrameWindowListener extends WindowAdapter
    {
        @Override
        public void windowClosing(WindowEvent e)
        {
            super.windowClosing(e);
            //
            conceal();
        }
    }
}
