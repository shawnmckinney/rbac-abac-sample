/*
 * © 2023 iamfortress.net
 */
package org.rbacabac;

import org.apache.directory.fortress.web.control.FtIndicatingAjaxButton;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;

/**
 * RbacAbac Sample Washer's Page
 *
 * @author Shawn McKinney
 * @version $Rev$
 */
public class WashersPage extends WicketSampleBasePage
{
    private String userId = getUserid();

    public WashersPage()
    {
        add( new WashersPageForm( "pageForm" ) );
    }

    /**
     * Page 1 Form
     */
    public class WashersPageForm extends Form
    {
        WashersPageForm( String id )
        {
            super( id );

            add( new Label( "label1", "Welcome " + userId + " of " + getBranchId() + " branch" ) );
            add( new FtIndicatingAjaxButton( "currency.soak" )
            {
                @Override
                protected void onSubmit( AjaxRequestTarget target )
                {
                    logIt( target, "Currency, Soak Pressed" );
                }
            } );

            add( new FtIndicatingAjaxButton( "currency.rinse" )
            {
                @Override
                protected void onSubmit( AjaxRequestTarget target )
                {
                    logIt( target, "Currency, Rinse Pressed" );
                }
            } );

            add( new FtIndicatingAjaxButton( "currency.dry" )
            {
                @Override
                protected void onSubmit( AjaxRequestTarget target )
                {
                    logIt( target, "Currency, Dry Pressed" );
                }
            } );
        }
    }
}
