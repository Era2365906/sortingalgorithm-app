import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.List;

public class SortingPerformanceApp extends JFrame {

    private JButton uploadBtn, runAllBtn, visualizeBtn;
    private JComboBox<String> columnSelector, algoSelector;
    private JTextArea resultsArea;
    private JTable dataTable, stepTable;
    private DefaultTableModel tableModel, stepTableModel;
    private List<String[]> csvData = new ArrayList<>();
    private Map<String, Double> performanceData = new HashMap<>();
    private ChartPanel chartPanel;
    private String bestAlgo = "";
    private String slowestAlgo = "";

    public SortingPerformanceApp() {
        setTitle("Sorting Algorithm Performance & Visualizer");
        setSize(1350, 950);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(200, 230, 255)); // light blue background

        // ---------- TOP PANEL ----------
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(new Color(200, 230, 255));

        uploadBtn = new JButton("Upload CSV File");
        uploadBtn.setBackground(Color.YELLOW);
        runAllBtn = new JButton("Run Performance Comparison");
        runAllBtn.setBackground(Color.YELLOW);
        visualizeBtn = new JButton("Visualize Steps");
        visualizeBtn.setBackground(Color.YELLOW);

        columnSelector = new JComboBox<>();
        algoSelector = new JComboBox<>(new String[]{
                "Insertion Sort", "Shell Sort", "Merge Sort", "Quick Sort", "Heap Sort"
        });

        topPanel.add(uploadBtn);
        topPanel.add(new JLabel("Column:"));
        topPanel.add(columnSelector);
        topPanel.add(new JLabel("Algorithm:"));
        topPanel.add(algoSelector);
        topPanel.add(visualizeBtn);
        topPanel.add(runAllBtn);

        // ---------- TABLE PANEL ----------
        JPanel tablePanel = new JPanel(new GridLayout(2, 1));
        tablePanel.setBackground(new Color(200, 230, 255));

        tableModel = new DefaultTableModel();
        dataTable = new JTable(tableModel);

        stepTableModel = new DefaultTableModel();
        stepTable = new JTable(stepTableModel);

        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer();
        headerRenderer.setHorizontalAlignment(JLabel.CENTER);
        dataTable.getTableHeader().setDefaultRenderer(headerRenderer);
        stepTable.getTableHeader().setDefaultRenderer(headerRenderer);

        tablePanel.add(new JScrollPane(dataTable));
        tablePanel.add(new JScrollPane(stepTable));

        // ---------- CHART PANEL ----------
        chartPanel = new ChartPanel();
        chartPanel.setPreferredSize(new Dimension(500, 350));
        chartPanel.setBorder(BorderFactory.createTitledBorder("Performance Bar Chart"));

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(new Color(200, 230, 255));
        centerPanel.add(tablePanel, BorderLayout.CENTER);
        centerPanel.add(chartPanel, BorderLayout.EAST);

        // ---------- RESULT AREA ----------
        resultsArea = new JTextArea(8, 50);
        resultsArea.setFont(new Font("Monospaced", Font.BOLD, 14));
        resultsArea.setEditable(false);
        resultsArea.setBorder(BorderFactory.createTitledBorder("Results"));

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(new JScrollPane(resultsArea), BorderLayout.SOUTH);

        // ---------- ACTIONS ----------
        uploadBtn.addActionListener(e -> uploadCSV());
        runAllBtn.addActionListener(e -> {
            performSortingAnalysis();
            chartPanel.repaint();
        });
        visualizeBtn.addActionListener(e -> visualizeSteps());
    }

    // ================= CHART PANEL =================
    class ChartPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (performanceData.isEmpty()) return;

            Graphics2D g2 = (Graphics2D) g;
            int width = getWidth();
            int height = getHeight();
            int padding = 70;
            int baseLine = height - padding;

            String[] algos = {"Insertion", "Shell", "Merge", "Quick", "Heap"};
            int barCount = algos.length;
            int barWidth = (width - 2 * padding) / barCount - 15;

            double maxTime = Collections.max(performanceData.values());
            double scale = (height - 2.0 * padding) / maxTime;

            g2.setColor(Color.BLACK);
            g2.drawLine(padding, padding, padding, baseLine);
            g2.drawLine(padding, baseLine, width - padding, baseLine);

            int divisions = 5;
            for (int i = 0; i <= divisions; i++) {
                int y = baseLine - (int) ((height - 2.0 * padding) / divisions * i);
                double value = (maxTime / divisions) * i;
                g2.drawLine(padding - 5, y, padding + 5, y);
                g2.drawString(String.format("%.2f", value), 10, y + 5);
            }

            int x = padding + 20;
            for (String algo : algos) {
                double time = performanceData.get(algo);
                int barHeight = (int) (time * scale);

                if (algo.equals(bestAlgo)) g2.setColor(new Color(0, 200, 0)); // green
                else if (algo.equals(slowestAlgo)) g2.setColor(new Color(200, 0, 0)); // red
                else g2.setColor(new Color(255, 180, 0)); // orange

                g2.fillRect(x, baseLine - barHeight, barWidth, barHeight);
                g2.setColor(Color.BLACK);
                g2.drawRect(x, baseLine - barHeight, barWidth, barHeight);

                g2.drawString(algo, x, baseLine + 18);
                g2.drawString(String.format("%.3f ms", time),
                        x, baseLine - barHeight - 5);

                x += barWidth + 25;
            }

            g2.drawString("Execution Time (ms)", 10, padding - 15);
            g2.drawString("Sorting Algorithms", width / 2 - 60, height - 15);
        }
    }

    // ================= CSV UPLOAD =================
    private void uploadCSV() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (BufferedReader br = new BufferedReader(new FileReader(chooser.getSelectedFile()))) {
                csvData.clear();
                columnSelector.removeAllItems();
                tableModel.setRowCount(0);

                String headerLine = br.readLine();
                if (headerLine == null) return;
                String[] headers = headerLine.split(",");
                tableModel.setColumnIdentifiers(headers);
                for (String h : headers) columnSelector.addItem(h);

                String line;
                while ((line = br.readLine()) != null) {
                    String[] row = line.split(",");
                    csvData.add(row);
                    tableModel.addRow(row);
                }
                JOptionPane.showMessageDialog(this, "CSV Uploaded Successfully");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }
    }

    // ================= STEP VISUALIZATION =================
    private void visualizeSteps() {
        int col = columnSelector.getSelectedIndex();
        if (col == -1 || csvData.isEmpty()) return;
        try {
            double[] data = csvData.stream()
                    .limit(8)
                    .mapToDouble(r -> Double.parseDouble(r[col]))
                    .toArray();
            stepTableModel.setRowCount(0);
            stepTableModel.setColumnIdentifiers(new String[]{"Step", "Array"});
            String algo = (String) algoSelector.getSelectedItem();
            stepTableModel.addRow(new Object[]{"Original", Arrays.toString(data)});
            if (algo.equals("Insertion Sort")) insertionSortSteps(data);
            else if (algo.equals("Shell Sort")) shellSortSteps(data);
            else if (algo.equals("Merge Sort")) mergeSortSteps(data, 0, data.length - 1);
            else if (algo.equals("Quick Sort")) quickSortSteps(data, 0, data.length - 1);
            else if (algo.equals("Heap Sort")) heapSortSteps(data);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please select numeric column");
        }
    }

    // ================= SORTING STEPS =================
    private void insertionSortSteps(double[] a) {
        for (int i = 1; i < a.length; i++) {
            double key = a[i]; int j = i-1;
            while(j>=0 && a[j]>key){ a[j+1]=a[j]; j--;}
            a[j+1]=key;
            stepTableModel.addRow(new Object[]{"Pass " + i, Arrays.toString(a)});
        }
    }
    private void shellSortSteps(double[] a){
        for(int gap=a.length/2;gap>0;gap/=2) for(int i=gap;i<a.length;i++){ double temp=a[i]; int j; for(j=i;j>=gap && a[j-gap]>temp;j-=gap) a[j]=a[j-gap]; a[j]=temp; stepTableModel.addRow(new Object[]{"Gap "+gap, Arrays.toString(a)});}
    }
    private void mergeSortSteps(double[] a,int l,int r){ if(l<r){ int m=(l+r)/2; mergeSortSteps(a,l,m); mergeSortSteps(a,m+1,r); merge(a,l,m,r); stepTableModel.addRow(new Object[]{"Merge "+l+"-"+r, Arrays.toString(a)});} }
    private void quickSortSteps(double[] a,int low,int high){ if(low<high){ int p=partition(a,low,high); stepTableModel.addRow(new Object[]{"Pivot "+p, Arrays.toString(a)}); quickSortSteps(a,low,p-1); quickSortSteps(a,p+1,high);} }
    private void heapSortSteps(double[] a){ int n=a.length; for(int i=n/2-1;i>=0;i--) heapify(a,n,i); stepTableModel.addRow(new Object[]{"Build Heap", Arrays.toString(a)}); for(int i=n-1;i>0;i--){ double t=a[0]; a[0]=a[i]; a[i]=t; heapify(a,i,0); stepTableModel.addRow(new Object[]{"Extract", Arrays.toString(a)});} }

    // ================= PERFORMANCE =================
    private void performSortingAnalysis() {
        int col = columnSelector.getSelectedIndex();
        if (col == -1 || csvData.isEmpty()) return;
        try {
            double[] original = csvData.stream().mapToDouble(r -> Double.parseDouble(r[col])).toArray();
            performanceData.clear(); bestAlgo=""; slowestAlgo="";
            StringBuilder sb = new StringBuilder("PERFORMANCE REPORT\n");
            String[] algos={"Insertion","Shell","Merge","Quick","Heap"};
            double minTime=Double.MAX_VALUE, maxTime=Double.MIN_VALUE;

            for(String algo:algos){
                double[] copy=original.clone();
                long start=System.nanoTime();
                switch (algo) {
                    case "Insertion":
                        insertionSort(copy);
                        break;
                    case "Shell":
                        shellSort(copy);
                        break;
                    case "Merge":
                        mergeSort(copy,0,copy.length-1);
                        break;
                    case "Quick":
                        quickSort(copy,0,copy.length-1);
                        break;
                    case "Heap":
                        heapSort(copy);
                        break;
                }
                double time=(System.nanoTime()-start)/1_000_000.0;
                performanceData.put(algo,time);
                if(time<minTime){ minTime=time; bestAlgo=algo;}
                if(time>maxTime){ maxTime=time; slowestAlgo=algo;}
                sb.append(algo).append(" Sort: ").append(String.format("%.3f ms\n",time));
            }

            sb.append("\nBEST ALGORITHM: ").append(bestAlgo).append(" SORT in ").append(String.format("%.3f ms",minTime));
            resultsArea.setText(sb.toString());

        }catch(Exception e){ JOptionPane.showMessageDialog(this,"Performance error"); }
    }

    // ================= BASIC SORT LOGIC =================
    private void insertionSort(double[] a) { for(int i=1;i<a.length;i++){ double key=a[i]; int j=i-1; while(j>=0 && a[j]>key){a[j+1]=a[j]; j--;} a[j+1]=key;} }
    private void shellSort(double[] a){ for(int gap=a.length/2;gap>0;gap/=2) for(int i=gap;i<a.length;i++){ double temp=a[i]; int j; for(j=i;j>=gap && a[j-gap]>temp;j-=gap) a[j]=a[j-gap]; a[j]=temp;} }
    private void mergeSort(double[] a,int l,int r){ if(l<r){ int m=(l+r)/2; mergeSort(a,l,m); mergeSort(a,m+1,r); merge(a,l,m,r);} }
    private void merge(double[] a,int l,int m,int r){ double[] L=Arrays.copyOfRange(a,l,m+1); double[] R=Arrays.copyOfRange(a,m+1,r+1); int i=0,j=0,k=l; while(i<L.length && j<R.length) a[k++]=(L[i]<=R[j]?L[i++]:R[j++]); while(i<L.length) a[k++]=L[i++]; while(j<R.length) a[k++]=R[j++]; }
    private void quickSort(double[] a,int low,int high){ if(low<high){ int p=partition(a,low,high); quickSort(a,low,p-1); quickSort(a,p+1,high);} }
    private int partition(double[] a,int low,int high){ double pivot=a[high]; int i=low-1; for(int j=low;j<high;j++){ if(a[j]<pivot){i++; double t=a[i]; a[i]=a[j]; a[j]=t;}} double t=a[i+1]; a[i+1]=a[high]; a[high]=t; return i+1; }
    private void heapSort(double[] a){ int n=a.length; for(int i=n/2-1;i>=0;i--) heapify(a,n,i); for(int i=n-1;i>0;i--){ double t=a[0]; a[0]=a[i]; a[i]=t; heapify(a,i,0);} }
    private void heapify(double[] a,int n,int i){ int largest=i,l=2*i+1,r=2*i+2; if(l<n && a[l]>a[largest]) largest=l; if(r<n && a[r]>a[largest]) largest=r; if(largest!=i){ double t=a[i]; a[i]=a[largest]; a[largest]=t; heapify(a,n,largest);} }

    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> new SortingPerformanceApp().setVisible(true));
    }
}
