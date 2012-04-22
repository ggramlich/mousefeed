package com.mousefeed.eclipse.popup;

import java.util.LinkedList;
import java.util.List;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

public class ShowShortcutPositioner {

    private List<PopUpData> popUps = new LinkedList<PopUpData>(); 
    
    public synchronized void register(PositionablePopUp popUp, Rectangle displayBounds, Point popUpSize) {
        popUps.add(new PopUpData(popUp, displayBounds, popUpSize));
        pushUp();
    }

    private void pushUp() {
        for (int i = popUps.size() - 2; i >= 0; i--) {
            PopUpData popUpData = popUps.get(i);
            popUpData.pushUp(popUps.get(i + 1).position);
            popUpData.popUp.setLocation(popUpData.position);
        }
    }

    public synchronized Point getPosition(PositionablePopUp popUp) {
        PopUpData popUpData = getPopUpData(popUp);
        return popUpData.position;
    }

    public synchronized void deRegister(PositionablePopUp popUp) {
        int index = getPopUpIndex(popUp);
        popUps.remove(index);
    }

    private int getPopUpIndex(PositionablePopUp popUp) {
        for (int i = 0; i < popUps.size(); i++) {
            if (popUps.get(i).popUp.equals(popUp)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Could not find registered Popup");
    }

    private PopUpData getPopUpData(PositionablePopUp popUp) {
        return popUps.get(getPopUpIndex(popUp));
    }

    private class PopUpData {
        private static final int VERTICAL_DISTANCE = 5;
        private final PositionablePopUp popUp;
        private final Point size;
        private final Point position;

        public PopUpData(PositionablePopUp popUp, Rectangle bounds, Point size) {
            this.popUp = popUp;
            this.size = size;
            position = new Point(getLowerRight(bounds).x - size.x, getLowerRight(bounds).y - size.y);
        }
        
        public void pushUp(Point positionBelow) {
            position.y = positionBelow.y - size.y - VERTICAL_DISTANCE;
        }

        private Point getLowerRight(Rectangle bounds) {
            return new Point(bounds.x + bounds.width, bounds.y + bounds.height);
        }
    }

}
