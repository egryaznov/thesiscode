package foundation.main;

import foundation.entity.MaritalBond;
import foundation.entity.Person;
import org.jdatepicker.JDatePicker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
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
        setSaveChangesBtnListener( new CreateProfileBtnListener(newNodeX, newNodeY) );
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
        setSaveChangesBtnListener( new UpdateProfileBtnListener(selectedPerson) );
        // Load first and last name from the node
        firstNameEdit.setText( selectedPerson.getFirstName() );
        lastNameEdit.setText( selectedPerson.getLastName() );
        // Load sex from the node
        maleCheckBox.setSelected( selectedPerson.isMale() );
        datePicker.getFormattedTextField().setValue( Person.stringToCalendar(selectedPerson.getDateOfBirth()) );
        // Load other fields
        occupationEdit.setText( selectedPerson.getOccupation() );
        phoneEdit.setText( selectedPerson.getPhone() );
        emailEdit.setText( selectedPerson.getEmail() );
        // Retrieving marriage information from model
        final @Nullable MaritalBond marriage = view.getModel().getMaritalBond(selectedPerson);
        if ( marriage != null )
        {
            // Setting the first name, last name and the wedding date
            final @NotNull Person spouse = ( marriage.getHead().equals(selectedPerson) )? marriage.getTail() : marriage.getHead();
            spouseNameLabel.setText(spouse.getFirstName() + " " + spouse.getLastName());
            dateOfMarriageLabel.setText( marriage.getDateOfWedding() );
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
        final String washedValue = Database.instance.wash( value );
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
        final @NotNull Calendar birthDate = (Calendar)datePicker.getFormattedTextField().getValue();
        column.put(Database.DATE_OF_BIRTH_COLUMN, Person.calendarToString(birthDate));
        // Put sex
        final String sex = ( maleCheckBox.isSelected() )? Database.MALE : Database.FEMALE;
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
        final String firstName = Database.instance.wash( firstNameEdit.getText() );
        final String lastName = Database.instance.wash( lastNameEdit.getText() );
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
            if ( profileInvalid() )
            {
                notifyProfileInvalid();
            }
            else
            {
                final @NotNull Map<String, String> row = gatherInfo();
                // Update the node itself
                personToUpdate.setFirstName( row.get(Database.FIRST_NAME_COLUMN) );
                personToUpdate.setLastName( row.get(Database.LAST_NAME_COLUMN) );
                personToUpdate.setSex( row.get(Database.SEX_COLUMN) );
                personToUpdate.setDateOfBirth( row.get(Database.DATE_OF_BIRTH_COLUMN) );
                personToUpdate.setOccupation( row.get(Database.OCCUPATION_COLUMN) );
                personToUpdate.setPhone( row.get(Database.PHONE_NUMBER_COLUMN) );
                personToUpdate.setEmail( row.get(Database.EMAIL_COLUMN) );
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