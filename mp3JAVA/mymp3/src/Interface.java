package src;
//Import the needed libraries
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileSystemView;
import javax.swing.*;
import java.awt.*;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;

import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.UnsupportedTagException;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;

public class Interface extends JFrame {
    //Declare buttons, File chooser, TextField, Mp3File, icon etc...
    private JButton play;
    private JButton next;
    private JButton stop;
    private JButton previous;
    private JButton open;
    private JButton playlist;
    private JFileChooser fileChooser;
    private JFileChooser dirs;
    private ArrayList<File> audioFiles;
    private int current;
    private AdvancedPlayer player;
    private FileSystemView fsv; 
    private Icon fileicon;
    private JLabel iconLabel;
    private Mp3File mp3file;
    private JTextField textfield;
    //Let's build our interface constructor
    public Interface() {
        //Lets name our interface
        super("MP3 Player");
        //Further costumization
        this.setResizable(false);
        this.setVisible(true);
        this.setPreferredSize(new Dimension(600, 600));
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());
        //Lets make a File system view 
        fsv = FileSystemView.getFileSystemView();
        //Let's make a panel which will be used to store our buttons
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 0, 0));
        //Further costumization of the front-end
        textfield = new JTextField("Welcome to my mp3! Select a song or a playlist");
        textfield.setPreferredSize(new Dimension(600,50));
        textfield.setHorizontalAlignment(SwingConstants.CENTER);
        textfield.setFont(new Font("Verdana", Font.BOLD, 10));
        textfield.setEditable(false);
        //Instantiating and naming the buttons
        this.stop = new JButton("Stop");
        this.play = new JButton("Play");
        this.next = new JButton("Next");
        this.previous = new JButton("Prev");
        this.open = new JButton("Song");
        this.playlist = new JButton("Playlist");
        //Setting the whole background to black
        getContentPane().setBackground(Color.BLACK);
        buttonPanel.setBackground(Color.BLACK);
        //Setting the buttons backgrounds to orange
        stop.setBackground(new Color(255,140,0));
        play.setBackground(new Color(255,140,0));
        next.setBackground(new Color(255,140,0));
        previous.setBackground(new Color(255,140,0));
        open.setBackground(new Color(255,140,0));
        playlist.setBackground(new Color(255,140,0));
        //Costumization of the buttons
        stop.setFont(new Font("Verdana", Font.BOLD, 14));
        play.setFont(new Font("Verdana", Font.BOLD, 14));
        next.setFont(new Font("Verdana", Font.BOLD, 14));
        previous.setFont(new Font("Verdana", Font.BOLD, 14));
        open.setFont(new Font("Verdana", Font.BOLD, 14));
        playlist.setFont(new Font("Verdana", Font.BOLD, 14));
        //Lets create our file and directory choser
        fileChooser = new JFileChooser();
        dirs = new JFileChooser();
        dirs.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        //We create an Arraylist that will be used to store our playlist's songs and an integer to keep track
        audioFiles = new ArrayList<>();
        current = -1;
        //Buttons costumization
        playlist.setPreferredSize(new Dimension(100, 70));
        previous.setPreferredSize(new Dimension(100, 70));
        play.setPreferredSize(new Dimension(100, 70));
        next.setPreferredSize(new Dimension(100, 70));
        open.setPreferredSize(new Dimension(100, 70));
        stop.setPreferredSize(new Dimension(100, 70));
        //Let's add an action listener to the "open" button 
        open.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int returnVal = fileChooser.showOpenDialog(null);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    //We select a file
                    File selectedFile = fileChooser.getSelectedFile();
                    try {
                        //We play the selected file
                        play(selectedFile);
                    } catch (Exception ex) {
                        //In case of an error we let the user know
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, "Error opening file: " + ex.getMessage());
                    }
                }
            }
        });
        //Let's add an action listener to our "playlist" button
        playlist.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //We take files and load them into our playlist with this action listener
                int returnVal = dirs.showOpenDialog(null);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File selectedDir = dirs.getSelectedFile();
                    loadPlaylist(selectedDir);
                }
            }
        });
        //Let's add an action listener to our "stop" button which calls the stop method once pressed
        stop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stop();
            }
        });
        //Let's add an action listener to the "next" button
        next.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //This condition will execute the play method of the current file only if 
                //the arraylist isnt empty or if we didnt exceed the size of the arraylist
                if (!audioFiles.isEmpty() && current < audioFiles.size() - 1) {
                    current++;
                    play(audioFiles.get(current));
                }
            }
        });
        //Let's add an action listener to the "previous" button
        previous.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //if the arraylist is not empty and the current variable is bigger than 0 which indicates we can play even the first song of our playlist
                if (!audioFiles.isEmpty() && current > 0) {
                    current--;
                    play(audioFiles.get(current));
                }
            }
        });
        //Let's add an action listener to the play button
        play.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (player != null) {
                    player.close();
                }
                //This condition will either play if we are not out of bounds for the playlists size which can take 1 mp3 file or even a playlist 
                if (current >= 0 && current < audioFiles.size()) {
                    play(audioFiles.get(current));
                } else {
                    //Or else it will display the error no audio file selected
                    JOptionPane.showMessageDialog(null, "No audio file selected.");
                }
            }
        });
        //Let's add these buttons to our button panel
        buttonPanel.add(previous);
        buttonPanel.add(play);
        buttonPanel.add(stop);
        buttonPanel.add(next);
        buttonPanel.add(open);
        buttonPanel.add(playlist);
        //Add our button panel to the frame with the textfield
        this.add(buttonPanel, BorderLayout.PAGE_END);
        this.add(textfield,BorderLayout.PAGE_START);
        this.pack();
    }
    
    private void play(File file) {
        try {
            //Initialized a Mp3File with the file's absolute path
            mp3file = new Mp3File(file.getAbsolutePath());
            //Closing the current player if it exists
            if (player != null) {
                player.close();
            }
            //We create a FileInputStream from the file
            FileInputStream fileInputStream = new FileInputStream(file);
            //Initialized a player with the FileInputStream
            player = new AdvancedPlayer(fileInputStream);
            //Making a thread to play the audio
            new Thread(() -> {
                try {
                    //Playing the audio
                    player.play();
                } catch (JavaLayerException e) {
                    //If there are issues playing the audio we let the user know
                    JOptionPane.showMessageDialog(null, "Error playing audio: " + e.getMessage());
                    e.printStackTrace();
                }
            }).start();
            //Getting the file's system icon
            fileicon = fsv.getSystemIcon(file);
            //Created a JLabel to put the file icon
            iconLabel = new JLabel(fileicon);
            //Made a BufferedImage to hold the icon image
            BufferedImage bi = new BufferedImage(fileicon.getIconWidth(),fileicon.getIconHeight(),BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = bi.createGraphics();
            //Painted the icon on the BufferedImage
            fileicon.paintIcon(null,graphics,0,0);
            graphics.dispose();
            //Costumized the resized icon
            int width = 100;
            int height = 100;
            Image resize = bi.getScaledInstance(width,height,Image.SCALE_SMOOTH);
            ImageIcon iconresize = new ImageIcon(resize);
            //Remove icon label if it exists
            if(iconLabel != null){
                this.remove(iconLabel);
            }
            //Passing the resized icon to the label and costumizing it
            iconLabel = new JLabel(iconresize);
            this.add(iconLabel,BorderLayout.CENTER);
            //Revalidating and repainting to make the changes noticeable 
            this.revalidate();
            this.repaint();
            //showing the song being currently played via a message dialog and displaying it in our textfield
            JOptionPane.showMessageDialog(null, "Now playing: " + file.getName());
            textfield.setText("Playing: " + file.getName());
            //In case of an error we let the user know
        } catch (UnsupportedTagException | InvalidDataException | IOException | JavaLayerException e) {
            JOptionPane.showMessageDialog(null, "Error playing audio: " + e.getMessage());
            e.printStackTrace();
        }
    }
    //Let's create this method to stop the music that will close the player
    private void stop() {
        if (player != null) {
            player.close();
        }
    }
    //Let's create this method for loading files into our playlist
    private void loadPlaylist(File directory) {
        //We clear the whole array and reset the "current" value
        audioFiles.clear();
        current = -1;
        //We add every file that has an mp3 extension into our arraylist
        for (File file : directory.listFiles()) {
            if (file.isFile() && (file.getName().endsWith(".mp3"))) {
                audioFiles.add(file);
            }
        }
        //We display a message once the playlist is loaded
        if (!audioFiles.isEmpty()) {
            current = 0;
            JOptionPane.showMessageDialog(null, "Playlist loaded.");
            textfield.setText("Playlist loaded");
        } else {
            JOptionPane.showMessageDialog(null, "No audio files found in the directory.");
        }
    }
    //We create the main method and 
    public static void main(String[] args) {
        //Creating an instance of our interface
        Interface mp3Player = new Interface();
        //Make our window appear on the screen
        mp3Player.setVisible(true);
    }
}
