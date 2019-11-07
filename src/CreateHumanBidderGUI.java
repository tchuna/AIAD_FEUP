
import jade.core.AID;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.util.*;

class  CreateHumanBidderGUI extends JFrame {

	private BidderHuman humanBidderAgent;

	private JTextField priceField;

    CreateHumanBidderGUI(BidderHuman a) {
		super(a.getLocalName() + ": Place bid or Rest");

		humanBidderAgent = a;

		JPanel p = new JPanel();
		p.setLayout(new GridLayout(4, 4));
		p.add(new JLabel("Current Budget:"));
		p.add(new JLabel(String.valueOf( humanBidderAgent.getcurrentBudget()+"$")));

		p.add(new JLabel("Bid :"));
		priceField = new JTextField(10);
		p.add(priceField);

		JButton bidButton = new JButton("Bid");
		bidButton.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    try {
                        String price = priceField.getText().trim();
                        //send agent bid
                        priceField.setText("");
                    }
                    catch (Exception e) {

                      JOptionPane.showMessageDialog(null,JOptionPane.INFORMATION_MESSAGE);

                    }
                }
            } );
		p.add(bidButton);

		JButton restButton = new JButton("Refuse");
		restButton.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    try {
                        //agent refuse to bid ;
                        priceField.setText("");
                    }
                    catch (Exception e) {

                      JOptionPane.showMessageDialog(null,JOptionPane.INFORMATION_MESSAGE);                    }
                }
            } );
		p.add(restButton);

    p.add(new JLabel("My myItens:"));
		JComboBox box = new JComboBox(humanBidderAgent.getmyItens());
		p.add(box);

		getContentPane().add(p, BorderLayout.CENTER);

		// Make the agent terminate when the user closes
		addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    humanBidderAgent.doDelete();
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
}
