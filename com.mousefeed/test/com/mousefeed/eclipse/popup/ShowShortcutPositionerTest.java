package com.mousefeed.eclipse.popup;

import static org.junit.Assert.assertEquals;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.junit.Before;
import org.junit.Test;

public class ShowShortcutPositionerTest {

    private ShowShortcutPositioner positioner;
    private Rectangle displayBounds;
    private Point popUpSize;

    @Before public void setUp() {
        positioner = new ShowShortcutPositioner();
        displayBounds = new Rectangle(20, 10, 130, 90);
        popUpSize = new Point(30, 25);
    }

    @Test public void getPosition() {
        PositionablePopUp popUp = new PositionablePopUp() {
            public void setLocation(Point position) {}
        };
        positioner.register(popUp, displayBounds, popUpSize);
        Point position = positioner.getPosition(popUp);
        assertEquals(new Point(120, 75), position);
    }

    @Test public void getPosition_TwoPopupsSetsLocationOnFirstPopUp() {
        final Point thePosition = new Point(0, 0);
        PositionablePopUp popUp1 = new PositionablePopUp() {
            public void setLocation(Point position) {
                thePosition.x = position.x;
                thePosition.y = position.y;
            }
        };
        positioner.register(popUp1, displayBounds, popUpSize);
        
        PositionablePopUp popUp2 = new PositionablePopUp() {
            public void setLocation(Point position) {}
        };
        positioner.register(popUp2, displayBounds, popUpSize);
        Point position2 = positioner.getPosition(popUp2);
        assertEquals(new Point(120, 75), position2);
        assertEquals(new Point(120, 75 - 25 - 5), thePosition);
    }

    @Test public void firstPopUpIsNotPushedUpAfterBeingDeRegistered() {
        PositionablePopUp popUp1 = new PositionablePopUp() {
            public void setLocation(Point position) {
                throw new AssertionError("setLocation should not have been called");
            }
        };
        positioner.register(popUp1, displayBounds, popUpSize);
        
        PositionablePopUp popUp2 = new PositionablePopUp() {
            public void setLocation(Point position) {}
        };
        positioner.deRegister(popUp1);
        positioner.register(popUp2, displayBounds, popUpSize);
    }
}
