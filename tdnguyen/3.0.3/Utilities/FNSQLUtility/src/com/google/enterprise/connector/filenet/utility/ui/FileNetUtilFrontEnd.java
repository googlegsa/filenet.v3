package com.google.enterprise.connector.filenet.utility.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.basic.BasicPanelUI;

import com.google.enterprise.connector.filenet.utility.file.FileNetSQLSupport;

public class FileNetUtilFrontEnd {

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		createGUI();
	}

	private static void createGUI(){
		GridBagLayout gb = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;

		int innerPanelWidth = 425;
		int innerPanelHeight = 50;
		int testFieldSize = 35;
		int labelWidth = 125;
		int labelHeight = 20;


		FlowLayout fl = new FlowLayout(FlowLayout.LEFT);

		JFrame frame = new JFrame("FileNet SQL Query");
//		frame.setBackground(Color.WHITE);
//		frame.setForeground(Color.WHITE);
//		frame.setIconImage(new Image("C:\\Program Files\\GoogleConnectors\\Sharepoint87\\Scripts\\run_url_conn_script.ico"));
		frame.addWindowListener(new WindowAdapter(){
		      public void windowClosing(WindowEvent we){
		        System.exit(0);
		      }
		    });
		frame.setSize(800, 600);
		frame.setLayout(fl);
		JPanel panel0 = new JPanel();
		panel0.setLayout(gb);

		GridBagLayout innergb1 = new GridBagLayout();
		GridBagConstraints innergbc1 = new GridBagConstraints();
		innergbc1.anchor = GridBagConstraints.NORTHWEST;
		JPanel panel1 = new JPanel();
		panel1.setLayout(innergb1);
		panel1.setPreferredSize(new Dimension(innerPanelWidth, innerPanelHeight));

			JLabel label1 = new JLabel("Username *");
			label1.setPreferredSize(new Dimension(labelWidth, labelHeight));
			innergbc1.gridx = 0;
			innergbc1.gridy = 0;
			innergb1.setConstraints(label1, innergbc1);
			panel1.add(label1);

			final TextField textField1 = new TextField(testFieldSize);
			innergbc1.gridx = 1;
			innergbc1.gridy = 0;
			innergb1.setConstraints(textField1, innergbc1);
			panel1.add(textField1);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gb.setConstraints(panel1, gbc);
		panel0.add(panel1);

		JPanel panel2 = new JPanel();
		panel2.setLayout(innergb1);
		panel2.setPreferredSize(new Dimension(innerPanelWidth, innerPanelHeight));

			JLabel label2 = new JLabel("Password *");
			label2.setPreferredSize(new Dimension(labelWidth, labelHeight));
			innergbc1.gridx = 0;
			innergbc1.gridy = 0;
			innergb1.setConstraints(label2, innergbc1);
			panel2.add(label2);

			final TextField textField2 = new TextField(testFieldSize);
			textField2.setEchoChar('*');
			innergbc1.gridx = 1;
			innergbc1.gridy = 0;
			innergb1.setConstraints(textField2, innergbc1);
			panel2.add(textField2);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gb.setConstraints(panel2, gbc);
		panel0.add(panel2);

		JPanel panel3 = new JPanel();
		panel3.setPreferredSize(new Dimension(innerPanelWidth, innerPanelHeight));

			JLabel label3 = new JLabel("Object Store *");
			label3.setPreferredSize(new Dimension(labelWidth, labelHeight));
			innergbc1.gridx = 0;
			innergbc1.gridy = 0;
			innergb1.setConstraints(label3, innergbc1);
			panel3.add(label3);

			final TextField textField3 = new TextField(testFieldSize);
			innergbc1.gridx = 1;
			innergbc1.gridy = 0;
			innergb1.setConstraints(textField3, innergbc1);
			panel3.add(textField3);

		gbc.gridx = 0;
		gbc.gridy = 2;
		gb.setConstraints(panel3, gbc);
		panel0.add(panel3);

		JPanel panel4 = new JPanel();
		panel4.setPreferredSize(new Dimension(innerPanelWidth, innerPanelHeight));

			JLabel label4 = new JLabel("Content Engine URL *");
			label4.setPreferredSize(new Dimension(labelWidth, labelHeight));
			innergbc1.gridx = 0;
			innergbc1.gridy = 0;
			innergb1.setConstraints(label4, innergbc1);
			panel4.add(label4);

			final TextField textField4 = new TextField(testFieldSize);
			innergbc1.gridx = 1;
			innergbc1.gridy = 0;
			innergb1.setConstraints(textField4, innergbc1);
			panel4.add(textField4);

		gbc.gridx = 0;
		gbc.gridy = 3;
		gb.setConstraints(panel4, gbc);
		panel0.add(panel4);

		JPanel panel5 = new JPanel();
		panel5.setPreferredSize(new Dimension(innerPanelWidth, innerPanelHeight));

			JLabel label5 = new JLabel("Wasp Location *");
			label5.setPreferredSize(new Dimension(labelWidth, labelHeight));
			innergbc1.gridx = 0;
			innergbc1.gridy = 0;
			innergb1.setConstraints(label5, innergbc1);
			panel5.add(label5);

			final TextField textField5 = new TextField(testFieldSize);
			innergbc1.gridx = 1;
			innergbc1.gridy = 0;
			innergb1.setConstraints(textField5, innergbc1);
			panel5.add(textField5);

		gbc.gridx = 0;
		gbc.gridy = 4;
		gb.setConstraints(panel5, gbc);
		panel0.add(panel5);

		JPanel panel6 = new JPanel();
		panel6.setPreferredSize(new Dimension(innerPanelWidth, innerPanelHeight));

			JLabel label6 = new JLabel("Domain");
			label6.setPreferredSize(new Dimension(labelWidth, labelHeight));
			innergbc1.gridx = 0;
			innergbc1.gridy = 0;
			innergb1.setConstraints(label6, innergbc1);
			panel6.add(label6);

			final TextField textField6 = new TextField(testFieldSize);
			innergbc1.gridx = 1;
			innergbc1.gridy = 0;
			innergb1.setConstraints(textField6, innergbc1);
			panel6.add(textField6);

		gbc.gridx = 0;
		gbc.gridy = 5;
		gb.setConstraints(panel6, gbc);
		panel0.add(panel6);

		JPanel panel7 = new JPanel();
		panel7.setPreferredSize(new Dimension(innerPanelWidth, innerPanelHeight+50));

			JLabel label7 = new JLabel("SQL Query *");
			label7.setPreferredSize(new Dimension(labelWidth, labelHeight));
			innergbc1.gridx = 0;
			innergbc1.gridy = 0;
			innergb1.setConstraints(label7, innergbc1);
			panel7.add(label7);

			final TextArea textArea= new TextArea("",4,35,TextArea.SCROLLBARS_VERTICAL_ONLY);
			innergbc1.gridx = 1;
			innergbc1.gridy = 0;
			innergb1.setConstraints(textArea, innergbc1);
			panel7.add(textArea);

		gbc.gridx = 0;
		gbc.gridy = 6;
		gb.setConstraints(panel7, gbc);
		panel0.add(panel7);

		JPanel panel9 = new JPanel();
		panel9.setPreferredSize(new Dimension(innerPanelWidth, innerPanelHeight));

			JLabel label9 = new JLabel("Log File Path *");
			label9.setPreferredSize(new Dimension(labelWidth, labelHeight));
			innergbc1.gridx = 0;
			innergbc1.gridy = 0;
			innergb1.setConstraints(label9, innergbc1);
			panel9.add(label9);

			final TextField textField9 = new TextField(testFieldSize-10);
			innergbc1.gridx = 1;
			innergbc1.gridy = 0;
			innergb1.setConstraints(textField9, innergbc1);
			panel9.add(textField9);

			final JCheckBox checkBox_1 = new JCheckBox("Full Logs");
//			checkBox_1.setPreferredSize(new Dimension(labelWidth, labelHeight));
			innergbc1.gridx = 2;
			innergbc1.gridy = 0;
			innergb1.setConstraints(checkBox_1, innergbc1);
			panel9.add(checkBox_1);

		gbc.gridx = 0;
		gbc.gridy = 7;
		gb.setConstraints(panel9, gbc);
		panel0.add(panel9);

		JPanel panel10 = new JPanel();
		panel10.setPreferredSize(new Dimension(innerPanelWidth, innerPanelHeight));

			JLabel label10_1 = new JLabel("PageSize");
			label10_1.setPreferredSize(new Dimension(labelWidth-50, labelHeight));
			innergbc1.gridx = 0;
			innergbc1.gridy = 0;
			innergb1.setConstraints(label10_1, innergbc1);
			panel10.add(label10_1);

			final TextField textField10_1 = new TextField(2);
			textField10_1.setText("100");
			innergbc1.gridx = 1;
			innergbc1.gridy = 0;
			innergb1.setConstraints(textField10_1, innergbc1);
			panel10.add(textField10_1);

			JLabel label10_2 = new JLabel("FilterLevel");
			label10_2.setPreferredSize(new Dimension(labelWidth-50, labelHeight));
			innergbc1.gridx = 2;
			innergbc1.gridy = 0;
			innergb1.setConstraints(label10_2, innergbc1);
			panel10.add(label10_2);

			final TextField textField10_2 = new TextField(2);
			textField10_2.setText("1");
			textField10_2.setEnabled(false);
			innergbc1.gridx = 3;
			innergbc1.gridy = 0;
			innergb1.setConstraints(textField10_2, innergbc1);
			panel10.add(textField10_2);

			JLabel label10_3 = new JLabel("Continuable");
			label10_3.setPreferredSize(new Dimension(labelWidth-25, labelHeight));
			innergbc1.gridx = 4;
			innergbc1.gridy = 0;
			innergb1.setConstraints(label10_3, innergbc1);
			panel10.add(label10_3);

			final TextField textField10_3 = new TextField(2);
			textField10_3.setText("1");
			textField10_3.setEnabled(false);
			innergbc1.gridx = 5;
			innergbc1.gridy = 0;
			innergb1.setConstraints(textField10_3, innergbc1);
			panel10.add(textField10_3);

		gbc.gridx = 0;
		gbc.gridy = 8;
		gb.setConstraints(panel10, gbc);
		panel0.add(panel10);

		JPanel panel8 = new JPanel();
		panel8.setPreferredSize(new Dimension(innerPanelWidth, innerPanelHeight));

			JButton buttonOK = new JButton("Test Query");
			innergbc1.gridx = 1;
			innergbc1.gridy = 1;
			innergb1.setConstraints(buttonOK, innergbc1);
			panel8.add(buttonOK);

		gbc.gridx = 0;
		gbc.gridy = 9;
		gb.setConstraints(panel8, gbc);
		panel0.add(panel8);

		panel0.setUI(new BasicPanelUI());
		panel0.setFont(new Font("Arial", Font.BOLD, 10));
		frame.add(panel0);
		frame.pack();
		frame.setResizable(false);
        frame.setVisible(true);
        buttonOK.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				FileNetSQLSupport.runQuery(textField1.getText(), textField2.getText(), textField3.getText(),
						textField4.getText(), textField5.getText(), textField6.getText(), textArea.getText(),
						textField9.getText(), Integer.parseInt(textField10_1.getText()),
						Integer.parseInt(textField10_2.getText()), Integer.parseInt(textField10_3.getText()),
						checkBox_1.isSelected());
			}
		});
	}
}
