//package com.aiad;

import jade.core.AID;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class CreateAuctionGui extends JFrame {
    private AuctioneerAg myAgent;

    private JTextField titleField, priceField;

    CreateAuctionGui(AuctioneerAg a) {
        super(a.getLocalName());

        myAgent = a;
        JPanel p = new JPanel();
        p.setLayout(new GridLayout(2, 2));
        p.add(new JLabel("Item in Auction:"));
        titleField = new JTextField(15);
        p.add(titleField);
        p.add(new JLabel("Initial Price:"));
        priceField = new JTextField(15);
        p.add(priceField);
        getContentPane().add(p, BorderLayout.CENTER);

        JButton addButton = new JButton("Start");
        addButton.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                try {
                    String name = titleField.getText().trim();
                    int startingPice = Integer.parseInt(priceField.getText().trim());
                    myAgent.startAuction(name, startingPice);
                    finish();
                }
                catch (Exception e) {
                    JOptionPane.showMessageDialog(CreateAuctionGui.this, "Invalid values. "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } );
        p = new JPanel();
        p.add(addButton);
        getContentPane().add(p, BorderLayout.SOUTH);

        // Make the agent terminate when the user closes
        // the GUI using the button on the upper right corner
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                myAgent.doDelete();
            }
        } );

        setResizable(false);
    }

    public void showGui() {
        pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int centerX = (int)screenSize.getWidth() / 2;
        int centerY = (int)screenSize.getHeight() / 2;
        setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
        super.setVisible(true);
    }

    public void finish(){
        super.removeAll();
        super.setVisible(false);
    }
}
