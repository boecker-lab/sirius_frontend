package de.unijena.bioinf.tree_viewer;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.unijena.bioinf.ms.properties.PropertyManager;
import org.jdesktop.swingx.JXTitledSeparator;
import org.slf4j.LoggerFactory;

import de.unijena.bioinf.ms.frontend.core.SiriusProperties;
import de.unijena.bioinf.ms.gui.utils.TwoCloumnPanel;
import javafx.application.Platform;

public class TreeViewerSettings extends JFrame implements ItemListener,
                                               ActionListener {

    TwoCloumnPanel panel;
    TreeVisualizationPanel treePanel;
    TreeViewerBridge bridge;
    JButton addCustomBtn;
    JCheckBox nodeLabelCB;
    JCheckBox deviationColorCB;
    JCheckBox edgeLabelCB;
    JCheckBox lossColorCB;
    JComboBox<String> colorVariant;
    JComboBox<String> scheme2Box;
    JComboBox<String> scheme3Box;
    JComboBox<String> edgeLabelMode;
    JCheckBox colorBarCB;
    JButton saveSettingsBtn;
    JButton managePresetsBtn;
    Map<String, JCheckBox> nodeAnnotationCBoxes;
    List<String> customAnnotations;
    TreeConfig localConfig;
    String selectedPreset;

    class CustomAnnotation extends JPanel implements ActionListener,
                                          ItemListener{
        JCheckBox checkBox;
        JTextField textField;
        JButton removeBtn;
        CustomAnnotation(){
            super();
            checkBox = new JCheckBox();
            textField = new JTextField();
            removeBtn = new JButton("-");
            removeBtn.addActionListener(this);
            this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
            this.add(checkBox);
            this.add(textField);
            this.add(removeBtn);
            checkBox.addItemListener(this);
        }
        CustomAnnotation(String text){
            this();
            textField.setText(text);
            checkBox.setSelected(true);
        }
        boolean isSelected(){
            return checkBox.isSelected();
        }
        String getValue(){
            return textField.getText();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource().equals(removeBtn)) {
                checkBox.setSelected(false);
                this.setVisible(false);
                customAnnotations.remove(getValue());
                SwingUtilities.windowForComponent(this).pack();
                panel.remove(this);
            }
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            if (checkBox.isSelected()){
                customAnnotations.add(getValue());
            } else{
                customAnnotations.remove(getValue());
            }
            List<String> nodeAnnotations = getSelectedNodeAnnotations();
            localConfig.set("nodeAnnotations", nodeAnnotations.toArray(
                                new String[0]));
            // popup annotations contain all that are not selected
            localConfig.set("popupAnnotations", getUnselectedNodeAnnotations().
                            toArray(new String[0]));
            Platform.runLater(() ->{
                    bridge.settingsChanged();
                });
        }
    }


    public TreeViewerSettings(TreeVisualizationPanel treePanel){
        super("Customize");
        this.treePanel = treePanel;
        this.setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
        this.bridge = treePanel.jsBridge;
        this.localConfig = treePanel.localConfig;
        this.selectedPreset = PropertyManager.getProperty("customPreset", null, "Custom");

        this.panel = new TwoCloumnPanel();

        panel.add(new JXTitledSeparator("Node annotations"));
        nodeLabelCB = new JCheckBox("molecular formula");
        panel.add(nodeLabelCB);
        nodeAnnotationCBoxes = new HashMap<String, JCheckBox>();
        for (int i = 0; i < TreeViewerBridge.NODE_ANNOTATIONS.length; i++){
            String annotation = TreeViewerBridge.NODE_ANNOTATIONS[i];
            JCheckBox annotationCB = new JCheckBox(annotation);
            panel.add(annotationCB);
            nodeAnnotationCBoxes.put(TreeViewerBridge.NODE_ANNOTATIONS_IDS[i], annotationCB);
        }

        panel.add(new JXTitledSeparator("Edge annotations"));
        edgeLabelCB = new JCheckBox("molecular formula");
        panel.add(edgeLabelCB, 0, true);
        edgeLabelMode = new JComboBox<>(new String[]{"simple", "boxed", "angled"});
        panel.add(new JLabel("Labels:"), edgeLabelMode);

        panel.add(new JXTitledSeparator("Color settings"));
        colorVariant = new JComboBox<>(TreeViewerBridge.COLOR_VARIANTS_DESC);
        JLabel colorVariantLabel = new JLabel("Colors:");
        panel.add(colorVariantLabel, colorVariant);
        scheme2Box = new JComboBox<>(TreeViewerBridge.COLOR_SCHEMES_2);
        panel.add(new JLabel("Color scheme (2 colors):"), scheme2Box);
        scheme3Box = new JComboBox<>(TreeViewerBridge.COLOR_SCHEMES_3);
        panel.add(new JLabel("Color scheme (3 colors):"), scheme3Box);
        colorBarCB = new JCheckBox("color legend");
        panel.add(colorBarCB);
        lossColorCB = new JCheckBox("color losses");
        panel.add(lossColorCB, 0, true);
        deviationColorCB = new JCheckBox("color mass deviation");
        panel.add(deviationColorCB, 0, true);

        // NOTE: this *has* to be at the bottom, because custom
        // annotations can only be added at the bottom! (could
        // potentially be fixed if needed)
        panel.add(new JXTitledSeparator("Custom node annotations"));
        addCustomBtn = new JButton("Add custom annotation...");
        addCustomBtn.setToolTipText("Add annotation by ID (must be defined in "
                                    + "the underlying tree model)");
        panel.add(addCustomBtn);
        customAnnotations = new ArrayList<>();

        this.add(panel);

        JPanel buttons = new JPanel();
        saveSettingsBtn = new JButton("Save preset \"" + selectedPreset + "\"");
        buttons.add(saveSettingsBtn);
        managePresetsBtn = new JButton("Presets...");
        buttons.add(managePresetsBtn);
        this.add(buttons);

        saveSettingsBtn.addActionListener(this);
        managePresetsBtn.addActionListener(this);
        colorBarCB.addItemListener(this);
        scheme3Box.addItemListener(this);
        scheme2Box.addItemListener(this);
        edgeLabelCB.addItemListener(this);
        edgeLabelMode.addItemListener(this);
        nodeLabelCB.addItemListener(this);
        addCustomBtn.addActionListener(this);
        for (JCheckBox annotationCB : nodeAnnotationCBoxes.values())
            annotationCB.addItemListener(this);
        colorVariant.addActionListener(this);
        lossColorCB.addItemListener(this);
        deviationColorCB.addItemListener(this);

        updateConfig();

        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        this.pack();
    }

    public void toggleShow(){
        this.setVisible(!this.isVisible());
    }

    // sets component states according to settings
    public void updateConfig(){
        for (String setting : TreeConfig.SETTINGS){
            Object value = localConfig.get(setting);
            switch (setting){
            case "colorVariant":
                for (int i = 0; i < TreeViewerBridge.COLOR_VARIANTS_IDS.length;
                     i++)
                    if (TreeViewerBridge.COLOR_VARIANTS_IDS[i].equals(
                            (String) value))
                        colorVariant.setSelectedItem(TreeViewerBridge.
                                                     COLOR_VARIANTS_DESC[i]);
                break;
            case "colorScheme2":
                scheme2Box.setSelectedItem((String) value);
                break;
            case "colorScheme3":
                scheme3Box.setSelectedItem((String) value);
                break;
            case "colorBar":
                colorBarCB.setSelected((boolean) value);
                break;
            case "nodeAnnotations":
                JCheckBox checkBox;
                List<String> nodeAnnotations = Arrays.asList((String[]) value);
                for (String annotation : TreeViewerBridge.NODE_ANNOTATIONS_IDS){
                    checkBox = (JCheckBox) nodeAnnotationCBoxes.get(annotation);
                    checkBox.setSelected(nodeAnnotations.contains(annotation));
                }
                // iterate over all nodeAnnotations that don't
                // have a checkbox and add them as customAnnotations
                for (String annotation : nodeAnnotations){
                    if (!annotation.trim().equals("")
                        && !nodeAnnotationCBoxes.keySet().contains(annotation)
                        && !customAnnotations.contains(annotation)){
                        CustomAnnotation customAnnot = new CustomAnnotation(
                            annotation);
                        panel.remove(addCustomBtn);
                        panel.add(customAnnot);
                        panel.add(addCustomBtn);
                    }
                    // TODO: remove customAnnotations not in nodeAnnotations?
                }
                this.pack();
                this.validate();
                break;
            case "edgeLabels":
                edgeLabelCB.setSelected((boolean) value);
                break;
            case "edgeLabelMode":
                edgeLabelMode.setSelectedItem((String) value);
                break;
            case "nodeLabels":
                nodeLabelCB.setSelected((boolean) value);
                break;
            case "lossColors":
                lossColorCB.setSelected((boolean) value);
                break;
            case "deviationColors":
                deviationColorCB.setSelected((boolean) value);
                break;
            }
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        Component source = (Component) e.getSource();
        if (nodeAnnotationCBoxes.values().contains(source)){
            List<String> nodeAnnotations = getSelectedNodeAnnotations();
            localConfig.set("nodeAnnotations",
                            nodeAnnotations.toArray(new String[0]));
            // popup annotations contain all that are not selected
            localConfig.set("popupAnnotations", getUnselectedNodeAnnotations().
                            toArray(new String[0]));
        } else if (source.equals(nodeLabelCB)){
            localConfig.set("nodeLabels", nodeLabelCB.isSelected());
        } else if (source.equals(edgeLabelCB)){
            localConfig.set("edgeLabels", edgeLabelCB.isSelected());
        } else if (source.equals(scheme2Box)){
            localConfig.set("colorScheme2",
                            (String) scheme2Box.getSelectedItem());
        }else if (source.equals(scheme3Box)){
            localConfig.set("colorScheme3",
                            (String) scheme3Box.getSelectedItem());
        }else if (source.equals(colorBarCB)){
            localConfig.set("colorBar", colorBarCB.isSelected());
        } else if (source.equals(edgeLabelMode)){
            localConfig.set("edgeLabelMode",
                            (String) edgeLabelMode.getSelectedItem());
        } else if (source.equals(lossColorCB)){
            localConfig.set("lossColors", lossColorCB.isSelected());
        } else if (source.equals(deviationColorCB)){
            localConfig.set("deviationColors", deviationColorCB.isSelected());
        }
        Platform.runLater(() ->{
                bridge.settingsChanged();
            });
        // TODO: look at preset config to see whether this is already a preset
        // for (String setting : TreeConfig.SETTINGS){
        //     for (TreeVisualizationPanel.Preset preset
        //              : TreeVisualizationPanel.PRESETS){
        //         if (PropertyManager.getProperty("de.unijena.bioinf.tree_viewer."
        //                                      + preset.toString() + "."
        //                                      + setting)
        //             == localConfig.get(setting))}
        // }
    }

    List<String> getSelectedNodeAnnotations(){
        List<String> nodeAnnotations = new ArrayList<>();
        for (String annotation : TreeViewerBridge.NODE_ANNOTATIONS_IDS){
            if (((JCheckBox) nodeAnnotationCBoxes.get(annotation)).isSelected())
                nodeAnnotations.add(annotation);
        }
        for (String customAnnot : customAnnotations){
            nodeAnnotations.add(customAnnot);
        }
        return nodeAnnotations;
    }

    List<String> getUnselectedNodeAnnotations(){
        List<String> selectedAnnotations = getSelectedNodeAnnotations();
        List<String> unselectedAnnotations = new ArrayList<>();
        for (String annotation : TreeViewerBridge.NODE_ANNOTATIONS_IDS)
            if (!selectedAnnotations.contains(annotation))
                unselectedAnnotations.add(annotation);
        return unselectedAnnotations;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(addCustomBtn)){
            // TODO: limit the number of custom annotations?
            CustomAnnotation customAnnot = new CustomAnnotation();
            panel.remove(addCustomBtn);
            panel.add(customAnnot);
            panel.add(addCustomBtn);
            this.pack();
            this.validate();
        } else if (e.getSource().equals(saveSettingsBtn)){
            if (selectedPreset.equals("Default")){
                if (JOptionPane.showConfirmDialog(this, "Do you really want to overwrite the default preset?")
                    != 0)
                    return;
            }
            for (String setting : TreeConfig.SETTINGS){
                PropertyManager.setProperty(
                    "de.unijena.bioinf.tree_viewer."
                    + selectedPreset + "." + setting,
                    localConfig.getAsString(setting));
            }
            PropertyManager
                .setProperty("de.unijena.bioinf.tree_viewer.preset",
                             selectedPreset);
            PropertyManager
                .setProperty("de.unijena.bioinf.tree_viewer.presets",
                             localConfig.getAsString("presets"));
            new SwingWorker<Integer, String>() {
                @Override
                protected Integer doInBackground() throws Exception {
                    LoggerFactory.getLogger(this.getClass()).info(
                        "Saving settings to properties File");
                    SiriusProperties.SIRIUS_PROPERTIES_FILE().store();
                    return 1;
                }
            }.execute();
            treePanel.presetBox.setSelectedItem(selectedPreset);
        } else if (e.getSource().equals(managePresetsBtn)) {
            JFrame managePresetsFrame = new JFrame("Manage presets");
            managePresetsFrame.setLayout(new BoxLayout(managePresetsFrame
                                                       .getContentPane(),
                                                       BoxLayout.PAGE_AXIS));
            DefaultListModel<String> presetListModel = new DefaultListModel<>();
            for (String preset : (String[]) localConfig.get("presets"))
                presetListModel.addElement(preset);
            JLabel listHelpLabel = new JLabel("Select a preset");
            // listHelpLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            listHelpLabel.setHorizontalAlignment(JLabel.LEFT);
            JList<String> presetList = new JList<>(presetListModel);
            JPanel buttons = new JPanel();
            JButton addPresetBtn = new JButton("add");
            addPresetBtn.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        String presetName = (String)JOptionPane
                            .showInputDialog(managePresetsFrame,
                                             "Enter a name for the new preset:",
                                             "New custom preset");
                        if (presetName == null)
                            return;
                        presetListModel.addElement(presetName);
                        treePanel.presetBox.addItem(presetName);
                        String[] presets = new String[presetListModel.size()];
                        presetListModel.copyInto(presets);
                        localConfig.set("presets", presets);
                        managePresetsFrame.pack();
                        managePresetsFrame.validate();
                    }
                });
            JButton removePresetBtn = new JButton("remove");
            removePresetBtn.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        String presetName = presetList.getSelectedValue();
                        if (presetName == null)
                            return;
                        presetListModel.removeElement(presetName);
                        treePanel.presetBox.removeItem(presetName);
                        String[] presets = new String[presetListModel.size()];
                        presetListModel.copyInto(presets);
                        localConfig.set("presets", presets);
                        if (selectedPreset.equals(presetName)){
                            if (!selectedPreset.equals("Custom"))
                                selectedPreset = "Custom";
                        }
                        managePresetsFrame.pack();
                        managePresetsFrame.validate();
                    }
                });
            managePresetsFrame.add(listHelpLabel);
            managePresetsFrame.add(presetList);
            buttons.add(addPresetBtn);
            buttons.add(removePresetBtn);
            managePresetsFrame.add(buttons);
            managePresetsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            managePresetsFrame.pack();
            managePresetsFrame.validate();
            managePresetsFrame.setVisible(true);
            presetList.addListSelectionListener(new ListSelectionListener(){
                    public void valueChanged(ListSelectionEvent e) {
                        if (presetList.getSelectedValue() != null)
                            selectedPreset = presetList.getSelectedValue();
                        saveSettingsBtn.setText("Save preset \""
                                                + selectedPreset + "\"");
                    }
                });
        } else if (e.getSource() == colorVariant) {
            String variant = (String) colorVariant.getSelectedItem();
            for (int i = 0; i < TreeViewerBridge.COLOR_VARIANTS_DESC.length;
                 i++)
                if (TreeViewerBridge.COLOR_VARIANTS_DESC[i].equals(variant))
                    localConfig.set("colorVariant",
                                    TreeViewerBridge.COLOR_VARIANTS_IDS[i]);
            Platform.runLater(() -> {
                    bridge.settingsChanged();
                });
        }
    }

}
