package de.unijena.bioinf.ms.gui.actions;/**
 * Created by Markus Fleischauer (markus.fleischauer@gmail.com)
 * as part of the sirius_frontend
 * 28.01.17.
 */

import ca.odell.glazedlists.swing.AdvancedListSelectionModel;
import de.unijena.bioinf.projectspace.InstanceBean;
import org.slf4j.LoggerFactory;

import javax.swing.*;

/**
 * @author Markus Fleischauer (markus.fleischauer@gmail.com)
 */
public enum SiriusActions {

    COMPUTE(ComputeAction.class),
//    CANCEL_COMPUTE(CancelComputeAction.class),
    COMPUTE_ALL(ComputeAllAction.class),
//    CANCEL_ALL(CancelComputeAllAction.class),
    ORDER_BY_INDEX(OrderCompoundByIndex.class),
    ORDER_BY_MASS(OrderCompoundByMass.class),
    ORDER_BY_NAME(OrderCompoundByName.class),

    COMPUTE_CSI(ComputeCSIAction.class),
    COMPUTE_CSI_LOCAL(ComputeCSILocalAction.class),

    IMPORT_EXP(ImportCompoundAction.class),
    IMPORT_EXP_BATCH(ImportAction.class),
    EDIT_EXP(EditExperimentAction.class),
    DELETE_EXP(DeleteExperimentAction.class),
    REMOVE_FORMULA_EXP(RemoveFormulaAction.class),


    NEW_WS(ProjectCreateAction.class),
    LOAD_WS(ProjectOpenAction.class),
    SAVE_WS(ProjectSaveAction.class),
    EXPORT_WS(ProjectExportAction.class),

    SHOW_SETTINGS(ShowSettingsDialogAction.class),
    SHOW_BUGS(ShowBugReportDialogAction.class),
    SHOW_ABOUT(ShowAboutDialogAction.class),
    SHOW_JOBS(ShowJobsDialogAction.class),
    SHOW_DB(ShowDBDialogAction.class),

    CHECK_CONNECTION(CheckConnectionAction.class);

    public static final ActionMap ROOT_MANAGER = new ActionMap();
    public final Class<? extends Action> actionClass;

    public synchronized Action getInstance(final boolean createIfNull, final ActionMap map) {
        Action a = map.get(name());
        if (a == null && createIfNull) {
            try {
                a = actionClass.newInstance();
                map.put(name(), a);
            } catch (InstantiationException | IllegalAccessException e) {
                LoggerFactory.getLogger(this.getClass()).error("Could not load following Sirius Action: " + name(), e);
            }
        }
        return a;
    }

    public Action getInstance(final ActionMap map) {
        return getInstance(false, map);
    }

    public Action getInstance(boolean createIfNull) {
        return getInstance(createIfNull, ROOT_MANAGER);
    }

    public Action getInstance() {
        return getInstance(true, ROOT_MANAGER);
    }

    /*public static void initRootManager() {
        for (SiriusActions action : values()) {
            try {
                if (ROOT_MANAGER.get(action.name()) == null) {
                    Action actionInstance = action.actionClass.newInstance();
                    ROOT_MANAGER.put(action.name(), actionInstance);
                }
            } catch (InstantiationException | IllegalAccessException e) {
                LoggerFactory.getLogger(SiriusActions.class).error("Could not load following Sirius Action: " + action.name(), e);
            }
        }
    }
*/

    SiriusActions(Class<? extends Action> action) {
        this.actionClass = action;
    }

    public static boolean notComputingOrEmpty(AdvancedListSelectionModel<InstanceBean> selection) {
        return !isComputingOrEmpty(selection);
    }

    public static boolean isComputingOrEmpty(AdvancedListSelectionModel<InstanceBean> selection) {
        if (selection == null || selection.isSelectionEmpty())
            return true;
        return selection.getSelected().stream().anyMatch(InstanceBean::isComputing);
    }

    public static boolean notComputingOrEmptyFirst(AdvancedListSelectionModel<InstanceBean> selection) {
        return !isComputingOrEmptyFirst(selection);
    }

    public static boolean isComputingOrEmptyFirst(AdvancedListSelectionModel<InstanceBean> selection) {
        if (selection == null || selection.isSelectionEmpty())
            return true;
        return selection.getSelected().get(0).isComputing();
    }


}