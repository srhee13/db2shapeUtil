
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class Db2shpUtil extends JFrame {

    JMenuBar menubar;

    JMenu menu0,menu1,menu2,subMenu;

    JMenuItem item1,item2,item3,item4;

    JPanel jPanel,jPane2;

    JLabel jLabel1,jLabel2,jLabel3,jLabel4;

    JTextField textfield ,textfield2,textField3,textField4;

    JButton select,select2,btnOK;

    JComboBox comboBox;

    static JTextArea jta;

    File file = null;

    public Db2shpUtil(String s, int x, int y, int w, int h) {

        init(s);

        setLocation(x,y);

        setSize(w,h);

        setVisible(true);

        setLayout(new BorderLayout());

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        setLocationRelativeTo(null);//设置居中

        setResizable(false);
    }

    void init(String s) {

        setTitle(s);

        menubar = new JMenuBar();

        menu0 = new JMenu("菜单"); //菜单栏的第一个选项

        /*subMenu = new JMenu("难度等级");

        menu0.add(subMenu);

        subMenu.add(new JMenuItem("初级"));

        subMenu.add(new JMenuItem("中级"));

        subMenu.add(new JMenuItem("高级"));

        menu0.addSeparator();*/
//        item4 = new JMenuItem("清空");
        item3 = new JMenuItem("退出");
        item3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int option = JOptionPane.showConfirmDialog(null,"您确定要推出系统吗，再考虑考虑？");
                if(option == JOptionPane.YES_OPTION){
                    System.exit(0);
                }
            }
        });

        menu0.add(item3);
        menubar.add(menu0);

        menu2 = new JMenu("关于");
        item1 = new JMenuItem("作者：srhee");
        menu2.add(item1);

        menu2.addSeparator();
        item2 = new JMenuItem("版本：v1.0");
        menu2.add(item2);
        menubar.add(menu2);

        setJMenuBar(menubar);

        jPanel = new JPanel();
        jPanel.setLayout(null);

        select = new JButton("浏览db文件");
        select.setBounds(20,20,120,20);
        jPanel.add(select);

        textfield = new JTextField(20);
        textfield.setEditable(false);
        textfield.setBounds(150,20,300,20);
        jPanel.add(textfield);

        select.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectFile();
            }
        });

        select2 = new JButton("输出目录");
        select2.setBounds(20,50,120,20);
        select2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectFileFolder();
            }
        });

        jPanel.add(select2);

        textfield2 = new JTextField( 20);
        textfield2.setEditable(false);
        textfield2.setBounds(150,50,300,20);
        jPanel.add(textfield2);

        /*jLabel1 = new JLabel("输出shp名称",JLabel.CENTER);
        jLabel1.setBounds(20,80,120,20);
        jPanel.add(jLabel1);

        textField3 = new JTextField("myshapefile",20);
        textField3.setBounds(150,80,150,20);
        textField3.setHorizontalAlignment(JTextField.RIGHT);
        jPanel.add(textField3);

        jLabel4 = new JLabel(".shp",JLabel.CENTER);
        jLabel4.setBounds(300,80,50,20);
        jPanel.add(jLabel4);*/

        jLabel2 = new JLabel("文件类型",JLabel.CENTER);
        jLabel2.setBounds(20,80,120,20);
        jPanel.add(jLabel2);

        comboBox =new JComboBox();
        comboBox.addItem("点（point）");
        comboBox.addItem("线（polyline）");
        comboBox.addItem("面（polygon）");
        comboBox.setSelectedIndex(2);
        comboBox.setBounds(150,80,150,20);
        jPanel.add(comboBox);

        jLabel3 = new JLabel("坐标串字段名称",JLabel.CENTER);
        jLabel3.setBounds(20,110,120,20);
        jPanel.add(jLabel3);

        textField4 = new JTextField("POLYGON",20);
        textField4.setBounds(150,110,150,20);
        jPanel.add(textField4);

        btnOK = new JButton("确定");
        btnOK.setBounds(150,150,120,30);
        jPanel.add(btnOK);

        final ImageIcon imageIcon = new ImageIcon("cky.jpg");
        jta = new JTextArea() {
            Image image = imageIcon.getImage();
            Image grayImage = GrayFilter.createDisabledImage(image);
            {
                setOpaque(false);
            } // instance initializer
            public void paint(Graphics g) {
                g.drawImage(grayImage, 0, 0, this);
                super.paint(g);
            }
        };
        jta.append("说明：\n" +
                "       1、只支持一个db文件内多个或一个同种类型的数据，如：同为面数据，同为线数据或点数据，并且标识坐标串的名称相同且区分大小写；默认是面数据，标识坐标串字段名称为POLYGON；\n" +
                "       2、输出shapefile文件名称与db数据库表文件名称相同，若输出目录已有数据，名称相同则会覆盖。\n-------------------------------------------------------------------------------------------------------\n");
        jta.setBounds(20,200,430,200);
        jta.setEditable(false);
        jta.setLineWrap(true);
        JScrollPane scroll = new JScrollPane(jta);
        scroll.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
//        scroll.setViewport(jta);
        scroll.setBounds(20,200,430,200);
        jPanel.add(scroll);

        btnOK.addActionListener(new ActionListener() {
            /*这里重写event事件处理方法*/
            public void actionPerformed(ActionEvent e) {
                String importFile = textfield.getText();
                String outFolder = textfield2.getText();
                String filedName = textField4.getText().trim();

                Toolkit.getDefaultToolkit().beep();
                String message = "";
                if(importFile.trim().equals("")){
                    message = "请先选择需要转换的db文件！";
                    JOptionPane.showMessageDialog(null, message, "提示",JOptionPane.WARNING_MESSAGE);
                    jta.append("【警告】："+message+"\n");
                }else if(outFolder.trim().equals("")){
                    message = "请先选择需要转换的shapefile文件目录！";
                    JOptionPane.showMessageDialog(null, message, "提示",JOptionPane.WARNING_MESSAGE);
                    jta.append("【警告】："+message+"\n");
                }else if(filedName.trim().equals("")){
                    message = "请先输入db文件坐标串所在字段名称！";
                    JOptionPane.showMessageDialog(null, message, "提示",JOptionPane.WARNING_MESSAGE);
                    jta.append("【警告】："+message+"\n");
                } else{
                    File importDB = new File(importFile);
                    File exportFolder = new File(outFolder);
                    if(!importDB.exists()){
                        message = "当前选择的db文件不存在，请重新选择！";
                        JOptionPane.showMessageDialog(null, message, "提示",JOptionPane.WARNING_MESSAGE);
                        jta.append("【警告】："+message+"\n");
                    }else if(!exportFolder.exists()){
                        message = "当前选择的输出文件夹不存在，请重新选择！";
                        JOptionPane.showMessageDialog(null, message, "提示",JOptionPane.WARNING_MESSAGE);
                        jta.append("【警告】："+message+"\n");
                    }else {
                        String fileType = "POLYGON";
                        int index = comboBox.getSelectedIndex();
                        if (index ==0){
                            fileType = "POINT";
                        }else if(index == 1) {
                            fileType = "POLYLINE";
                        }else if(index == 2){
                            fileType = "POLYGON";
                        }
                        jta.append("【提示】：当前操作执行参数为["+importFile+" , "+outFolder+"  , "+fileType+" , "+filedName+" ].\n");
                        handleFile(importFile,outFolder,fileType,filedName);
                    }
                }
            }
        });
        setContentPane(jPanel);
    }


    public void selectFile() {
        //弹出文件选择框
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("选择db文件");     //自定义选择框标题
        chooser.setSelectedFile(new File("C:\\Users\\Administrator\\Desktop")); //设置默认文件名

        //后缀名过滤器
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "DB文件(*.db)", "db");
        chooser.setFileFilter(filter);

        //下面的方法将阻塞，直到【用户按下保存按钮且“文件名”文本框不为空】或【用户按下取消按钮】
        int option = chooser.showSaveDialog(null);
        if(option==JFileChooser.APPROVE_OPTION){	//假如用户选择了保存
            file = chooser.getSelectedFile();
            String filePath = chooser.getSelectedFile().getPath();
            String fname = chooser.getName(file);	//从文件名输入框中获取文件名
            //假如用户填写的文件名不带我们制定的后缀名，那么我们给它添上后缀
            if(fname.indexOf(".db")==-1){
                file=new File(chooser.getCurrentDirectory(),fname+".db");
                filePath += ".db";
                jta.append("【提示】：选择的文件不带扩展名db，已自动添加扩展名。\n");
            }
            textfield.setText(filePath);
            jta.append("【提示】：当前选择的文件路径是 "+filePath+" 。\n");
        }
    }

    public void selectFileFolder(){
        //弹出文件选择框
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("选择转换文件目录");     //自定义选择框标题
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setSelectedFile(new File("C:\\Users\\Administrator\\Desktop"));

        int option = chooser.showSaveDialog(null);
        if(option==JFileChooser.APPROVE_OPTION){	//假如用户选择了保存
            String filePath = chooser.getSelectedFile().getPath();
            textfield2.setText(filePath);
            jta.append("【提示】：当前选择的文件夹路径是 "+filePath+" \n");
        }else {
            jta.append("【警告】：未正确选择输出文件夹！ \n");
        }
    }

    public void handleFile(String importFile ,String outFolder,String fileType,String filedName) {
        System.out.println(file.getPath());
        if(file.getPath().equals(textfield.getText())){
            try {
                DbHandler db = new DbHandler(importFile);
                if(db.transformAll(outFolder,fileType,filedName)){
                    java.awt.Desktop.getDesktop().open(new File(outFolder));
                    jta.append("【提示】：文件转换完毕，请查看已打开的资源管理器。\n.......................................................\n");
                }else {
                    Toolkit.getDefaultToolkit().beep();
                    JOptionPane.showMessageDialog(null, "转换出错，请联系管理员！", "提示",JOptionPane.WARNING_MESSAGE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                jta.append("【失败】：参考信息如下......................................\n"+e.getMessage()+"\n");
            }
        }
    }

    //定义main方法
    public static void main(String[] args) {
        new Db2shpUtil("db转shp工具",200,200,480,480);
    }

}
