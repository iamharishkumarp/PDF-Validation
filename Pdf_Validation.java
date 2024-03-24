package harish;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;

@SuppressWarnings("serial")
public class Pdf_Validation extends JFrame {

	private JLabel folderPathLabel;
	private JButton chooseFolderButton;
	private JButton checkValidityButton;
	private JLabel statusLabel;
	private JFileChooser folderChooser;
	private JFileChooser saveFileChooser;
	private File selectedFolder;

	public Pdf_Validation() {
		super("PDF Metadata Validator");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(800, 480);
		getContentPane().setBackground(new Color(240, 240, 240));

		// Components
		folderPathLabel = new JLabel("Selected Folder:");
		folderPathLabel.setFont(new Font("Arial", Font.PLAIN, 14));
		folderPathLabel.setForeground(Color.BLACK);
		chooseFolderButton = new JButton("Choose Folder");
		chooseFolderButton.setBackground(new Color(255, 165, 0));
		chooseFolderButton.setForeground(Color.WHITE);
		checkValidityButton = new JButton("Check Validity");
		checkValidityButton.setBackground(new Color(255, 69, 0));
		checkValidityButton.setForeground(Color.WHITE);

		statusLabel = new JLabel();
		statusLabel.setFont(new Font("Arial", Font.BOLD, 20));
		statusLabel.setForeground(Color.BLACK);
		statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
		folderChooser = new JFileChooser();
		folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		saveFileChooser = new JFileChooser();
		saveFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		// Panel for folder path input
		JPanel inputPanel = new JPanel(new GridLayout(1, 2, 10, 10));
		inputPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
		inputPanel.setBackground(new Color(240, 240, 240));
		inputPanel.add(folderPathLabel);
		inputPanel.add(chooseFolderButton);

		// Panel for Check Validity button
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonPanel.setBackground(new Color(240, 240, 240));
		buttonPanel.add(checkValidityButton);

		// Adding components to the frame
		setLayout(new BorderLayout());
		add(inputPanel, BorderLayout.NORTH);
		add(buttonPanel, BorderLayout.CENTER);
		add(statusLabel, BorderLayout.SOUTH);
		// Action listener for choose folder button
		chooseFolderButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int returnValue = folderChooser.showOpenDialog(Pdf_Validation.this);
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					selectedFolder = folderChooser.getSelectedFile();
					folderPathLabel.setText("Selected Folder: " + selectedFolder.getAbsolutePath());
				}
			}
		});

		// Action listener for Check Validity button
		checkValidityButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					validatePDFsInFolder();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		});
	}

	private void validatePDFsInFolder() throws IOException {
		if (selectedFolder == null) {
			JOptionPane.showMessageDialog(this, "Please select a folder first.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		File[] files = selectedFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));
		if (files == null || files.length == 0) {
			JOptionPane.showMessageDialog(this, "No PDF files found in the selected folder.", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		List<String> names = new ArrayList<>();
		List<String> results = new ArrayList<>();
		for (File file : files) {
			boolean isValid = checkValidity(file);

			names.add(file.getName());
			results.add(isValid ? "Valid" : "Invalid");
		}

		saveResults(names, results);
		statusLabel.setText("Validation completed. Result saved.");
	}

	private boolean checkValidity(File file) throws IOException {
		try (PDDocument document = PDDocument.load(file)) {
			PDDocumentInformation info = document.getDocumentInformation();
			Date creationDate = info.getCreationDate() != null ? info.getCreationDate().getTime() : null;
			Date modificationDate = info.getModificationDate() != null ? info.getModificationDate().getTime() : null;

			return modificationDate == null || modificationDate.equals(creationDate);
		}
	}

	private void saveResults(List<String> names, List<String> results) {
		int returnValue = saveFileChooser.showSaveDialog(this); // Use 'this' instead of 'Testing.this'
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			File selectedFile = saveFileChooser.getSelectedFile();
			String outputPath = selectedFile.getAbsolutePath();

			try {
				writeResultPDF(names, results, outputPath);
				JOptionPane.showMessageDialog(this, "Results saved successfully.", "Success",
						JOptionPane.INFORMATION_MESSAGE);
			} catch (IOException ex) {
				ex.printStackTrace();
				JOptionPane.showMessageDialog(this, "Error saving the results.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void writeResultPDF(List<String> fileNames, List<String> results, String outputPath) throws IOException {
		if (!outputPath.toLowerCase().endsWith(".pdf")) {
			outputPath += ".pdf";
		}

		PdfWriter writer = new PdfWriter(outputPath);
		PdfDocument pdf = new PdfDocument(writer);
		Document document = new Document(pdf);

		// Create fonts
		PdfFont bold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
		PdfFont normal = PdfFontFactory.createFont(StandardFonts.HELVETICA);

		// Create table
		Table table = new Table(new float[] { 1, 3, 5 }); // 3 columns for SL. No., File Name, and Result

		// Add table header
		Cell cell = new Cell().add(new Paragraph(" SI.No. ").setFont(bold));
		cell.setTextAlignment(TextAlignment.CENTER);
		cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
		table.addCell(cell);

		cell = new Cell().add(new Paragraph("File Name").setFont(bold));
		cell.setTextAlignment(TextAlignment.CENTER);
		cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
		table.addCell(cell);

		cell = new Cell().add(new Paragraph(" Result ").setFont(bold));
		cell.setTextAlignment(TextAlignment.CENTER);
		cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
		table.addCell(cell);

		// Add data to table
		for (int i = 0; i < fileNames.size(); i++) {
			cell = new Cell().add(new Paragraph(String.format("%03d", i + 1)).setFont(normal));
			cell.setTextAlignment(TextAlignment.CENTER);
			cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
			table.addCell(cell);

			String name = fileNames.get(i) + " ".repeat(150 - fileNames.get(i).length());
			cell = new Cell().add(new Paragraph(name).setFont(normal));
			cell.setTextAlignment(TextAlignment.LEFT);
			cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
			table.addCell(cell);

			cell = new Cell().add(new Paragraph("  "+results.get(i)+"  ").setFont(normal));
			cell.setTextAlignment(TextAlignment.CENTER);
			cell.setVerticalAlignment(VerticalAlignment.MIDDLE);
			if (results.get(i).equalsIgnoreCase("valid")) {
				cell.setBackgroundColor(new DeviceRgb(144, 238, 144)); // Light green
			} else {
				cell.setBackgroundColor(new DeviceRgb(255, 99, 71)); // Tomato red
			}
			table.addCell(cell);
		}

		document.add(table);
		document.close();
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Pdf_Validation gui = new Pdf_Validation();
				gui.setVisible(true);
			}
		});
	}
}