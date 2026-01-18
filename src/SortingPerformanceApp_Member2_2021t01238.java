import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.List;

public class SortingPerformanceApp_Member2_2021t01238 extends JFrame {

    private JButton uploadBtn, runBtn, visualizeBtn;
    private JComboBox<String> columnSelector, algoSelector;
    private JTable table, stepTable;
    private DefaultTableModel model, stepModel;
    private JTextArea result;
    private List<String[]> csvData=new ArrayList<>();

    public SortingPerformanceApp_Member2_2021t01238(){
        setTitle("Sorting Performance - Member 2");
        setSize(1200,800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        uploadBtn=new JButton("Upload CSV");
        runBtn=new JButton("Run");
        visualizeBtn=new JButton("Visualize");

        columnSelector=new JComboBox<>();
        algoSelector=new JComboBox<>(new String[]{"Quick Sort","Heap Sort"});

        JPanel top=new JPanel();
        top.add(uploadBtn);
        top.add(columnSelector);
        top.add(algoSelector);
        top.add(visualizeBtn);
        top.add(runBtn);

        model=new DefaultTableModel();
        table=new JTable(model);

        stepModel=new DefaultTableModel();
        stepTable=new JTable(stepModel);

        result=new JTextArea(6,40);

        setLayout(new BorderLayout());
        add(top,BorderLayout.NORTH);
        add(new JScrollPane(table),BorderLayout.CENTER);
        add(new JScrollPane(stepTable),BorderLayout.EAST);
        add(new JScrollPane(result),BorderLayout.SOUTH);

        uploadBtn.addActionListener(e->upload());
        visualizeBtn.addActionListener(e->visualize());
        runBtn.addActionListener(e->runSort());
    }

    private void upload(){
        JFileChooser fc=new JFileChooser();
        if(fc.showOpenDialog(this)==JFileChooser.APPROVE_OPTION){
            try(BufferedReader br=new BufferedReader(new FileReader(fc.getSelectedFile()))){
                model.setRowCount(0);
                columnSelector.removeAllItems();
                String[] h=br.readLine().split(",");
                model.setColumnIdentifiers(h);
                for(String s:h) columnSelector.addItem(s);
                String l;
                while((l=br.readLine())!=null){
                    String[] r=l.split(",");
                    csvData.add(r);
                    model.addRow(r);
                }
            }catch(Exception e){}
        }
    }

    private void visualize(){
        int c=columnSelector.getSelectedIndex();
        double[] a=csvData.stream().limit(8).mapToDouble(r->Double.parseDouble(r[c])).toArray();
        stepModel.setColumnIdentifiers(new String[]{"Step","Array"});
        stepModel.setRowCount(0);
        if(algoSelector.getSelectedItem().equals("Quick Sort"))
            quickSteps(a,0,a.length-1);
        else heapSteps(a);
    }

    private void runSort(){
        int c=columnSelector.getSelectedIndex();
        double[] a=csvData.stream().mapToDouble(r->Double.parseDouble(r[c])).toArray();
        long s=System.nanoTime();
        if(algoSelector.getSelectedItem().equals("Quick Sort"))
            quick(a,0,a.length-1);
        else heap(a);
        result.setText("Time : "+(System.nanoTime()-s)/1_000_000.0+" ms");
    }

    private void quick(double[] a,int l,int h){
        if(l<h){
            int p=part(a,l,h);
            quick(a,l,p-1);
            quick(a,p+1,h);
        }
    }

    private int part(double[] a,int l,int h){
        double p=a[h]; int i=l-1;
        for(int j=l;j<h;j++)
            if(a[j]<p){i++; double t=a[i];a[i]=a[j];a[j]=t;}
        double t=a[i+1];a[i+1]=a[h];a[h]=t;
        return i+1;
    }

    private void heap(double[] a){
        for(int i=a.length/2-1;i>=0;i--) heapify(a,a.length,i);
        for(int i=a.length-1;i>0;i--){
            double t=a[0];a[0]=a[i];a[i]=t;
            heapify(a,i,0);
        }
    }

    private void heapify(double[] a,int n,int i){
        int l=2*i+1,r=2*i+2,lg=i;
        if(l<n&&a[l]>a[lg]) lg=l;
        if(r<n&&a[r]>a[lg]) lg=r;
        if(lg!=i){
            double t=a[i];a[i]=a[lg];a[lg]=t;
            heapify(a,n,lg);
        }
    }

    private void quickSteps(double[] a,int l,int h){
        if(l<h){
            int p=part(a,l,h);
            stepModel.addRow(new Object[]{"Pivot "+p,Arrays.toString(a)});
            quickSteps(a,l,p-1);
            quickSteps(a,p+1,h);
        }
    }

    private void heapSteps(double[] a){
        heap(a);
        stepModel.addRow(new Object[]{"Final",Arrays.toString(a)});
    }

    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> new SortingPerformanceApp_Member2_2021t01238().setVisible(true));
    }
}
