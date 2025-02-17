/*
 * Mars Simulation Project
 * MagnifierToggleTool.java
 * @date 2021-09-20
 * @author Manny Kung
 */

/*
 * This file is part of WebLookAndFeel library.
 *
 * WebLookAndFeel library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * WebLookAndFeel library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with WebLookAndFeel library.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mars_sim.msp.ui.swing.tool;

import com.alee.extended.layout.HorizontalFlowLayout;
import com.alee.extended.magnifier.MagnifierGlass;
import com.alee.laf.WebLookAndFeel;
import com.alee.laf.button.WebToggleButton;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.window.WebFrame;
import com.alee.managers.style.StyleId;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * {@link com.alee.demo.DemoApplication} magnifier glass.
 *
 * @author Mikle Garin
 */
@SuppressWarnings("serial")
public final class MagnifierToggleTool extends WebPanel
{
    /**
     * {@link MagnifierGlass} instance.
     */
    private final MagnifierGlass magnifier;

    /**
     * Constructs new {@link MagnifierToggleTool}.
     *
     * @param application {@link DemoApplication}
     */
    public MagnifierToggleTool (WebFrame frame)
    {
        super ( StyleId.panelTransparent, new HorizontalFlowLayout ( 0, false ) );

		frame.setIconImages(WebLookAndFeel.getImages());
		
        // Magnifier glass
        magnifier = new MagnifierGlass ();

        // Magnifier glass switcher button
        final WebToggleButton magnifierButton = new WebToggleButton ();// DemoStyles.toolButton, DemoIcons.magnifier16 );
        magnifierButton.setLanguage ( "demo.tool.magnifier" );
        magnifierButton.setSelected ( magnifier.isDisplayed () );
        magnifierButton.addActionListener ( new ActionListener ()
        {
            @Override
            public void actionPerformed ( final ActionEvent e )
            {
                magnifier.displayOrDispose ( frame );
            }
        } );
        add ( magnifierButton );

        // Dummy cursor display switcher button
        final WebToggleButton dummyCursorButton = new WebToggleButton ();// DemoStyles.toolIconButton, DemoIcons.cursor16 );
        dummyCursorButton.setLanguage ( "demo.tool.magnifier.cursor" );
        dummyCursorButton.setSelected ( magnifier.isDisplayDummyCursor () );
        dummyCursorButton.addActionListener ( new ActionListener ()
        {
            @Override
            public void actionPerformed ( final ActionEvent e )
            {
                magnifier.setDisplayDummyCursor ( !magnifier.isDisplayDummyCursor () );
            }
        } );
        add ( dummyCursorButton );
    }
}
