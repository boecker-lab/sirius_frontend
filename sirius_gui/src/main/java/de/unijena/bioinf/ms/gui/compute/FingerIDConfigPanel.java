package de.unijena.bioinf.ms.gui.compute;

import de.unijena.bioinf.ChemistryBase.chem.PrecursorIonType;
import de.unijena.bioinf.ChemistryBase.ms.PossibleAdducts;
import de.unijena.bioinf.chemdb.SearchableDatabase;
import de.unijena.bioinf.ms.frontend.subtools.fingerid.FingerIdOptions;
import de.unijena.bioinf.ms.gui.utils.GuiUtils;
import de.unijena.bioinf.ms.gui.utils.jCheckboxList.JCheckBoxList;
import de.unijena.bioinf.ms.gui.utils.jCheckboxList.JCheckboxListPanel;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Markus Fleischauer (markus.fleischauer@gmail.com)
 */

//here we can show fingerid options. If it becomes to much, we can change this to a setting like tabbed pane
public class FingerIDConfigPanel extends SubToolConfigPanel<FingerIdOptions> {
    //todo sync db selection with sirius panel

    protected final JCheckboxListPanel<SearchableDatabase> searchDBList;
    public final JCheckboxListPanel<String> adductOptions;
//    private ToolbarToggleButton csiButton = null;

    public FingerIDConfigPanel(final JCheckBoxList<String> sourceIonization) {
        super(FingerIdOptions.class);
//        JPanel target = this;
//        if (horizontal) {
           /* if (button) {
                setLayout(new FlowLayout(FlowLayout.LEFT));
                target = new JPanel();
                csiButton = new ToolbarToggleButton("CSI:FingerID", Icons.FINGER_32);
                csiButton.setPreferredSize(new Dimension(110, 60));
                csiButton.setMaximumSize(new Dimension(110, 60));
                csiButton.setMinimumSize(new Dimension(110, 60));
                MainFrame.CONNECTION_MONITOR.addConectionStateListener(evt -> setCsiButtonEnabled(((ConnectionMonitor.ConnectionStateEvent) evt).getConnectionCheck().isConnected()));
                setCsiButtonEnabled(MainFrame.MF.isFingerid());

                csiButton.addActionListener(e -> {
                    setComponentsEnabled(csiButton.isSelected());
                    csiButton.setToolTipText((csiButton.isSelected() ? "Disable CSI:FingerID search" : "Enable CSI:FingerID search"));
                });
                csiButton.setSelected(false);
                add(csiButton);
                add(target);
            }*/
//            RelativeLayout rl = new RelativeLayout(RelativeLayout.X_AXIS, 15);
//            rl.setAlignment(RelativeLayout.LEADING);
//            target.setLayout(rl);

       /* } else {
            target.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        }*/

        // configure database to search list
        searchDBList = new JCheckboxListPanel<>(new DBSelectionList(), "Search in DBs:");
        GuiUtils.assignParameterToolTip(searchDBList, "StructureSearchDB");
        parameterBindings.put("StructureSearchDB", () -> getStructureSearchDBs().stream().map(SearchableDatabase::name).
                collect(Collectors.joining(",")));
        add(searchDBList);

        adductOptions = new JCheckboxListPanel<>(new AdductSelectionList(sourceIonization), "Possible Adducts");
        parameterBindings.put("AdductSettings.detectable", () -> getSelectedAdducts().toString());
        add(adductOptions);
    }


    public PossibleAdducts getSelectedAdducts() {
        return adductOptions.checkBoxList.getCheckedItems().stream().map(PrecursorIonType::parsePrecursorIonType)
                .flatMap(Optional::stream).collect(Collectors.collectingAndThen(Collectors.toSet(), PossibleAdducts::new));
    }

    public List<SearchableDatabase> getStructureSearchDBs() {
        return searchDBList.checkBoxList.getCheckedItems();
    }
}
