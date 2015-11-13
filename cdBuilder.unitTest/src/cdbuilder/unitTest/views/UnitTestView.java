/* This file is created by Nan Yang in 01/19/2014
 * This class is a view to display unit test result
 */
package cdbuilder.unitTest.views;

import org.eclipse.swt.widgets.*;
import org.eclipse.ui.part.ViewPart;

public class UnitTestView extends ViewPart {
        public static Label label;
        
        public UnitTestView() {
                super();
                
        }
     public void setFocus() {
                label.setFocus();
        }
     public void createPartControl(Composite parent) {
                label = new Label(parent, 0);
                label.setText(infoHolder.getState()+"\n"+infoHolder.getText());
        }


}