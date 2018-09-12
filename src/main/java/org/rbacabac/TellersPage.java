/*
 * This is free and unencumbered software released into the public domain.
 */
package org.rbacabac;

import org.apache.directory.fortress.web.control.FtIndicatingAjaxButton;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;

/**
 * RbacAbac Sample Teller's Page
 *
 * @author Shawn McKinney
 * @version $Rev$
 */
public class TellersPage extends WicketSampleBasePage
{
    private String userId = getUserid();

    public TellersPage()
    {
        add( new TellersPageForm( "pageForm" ) );
    }

    /**
     * Page 1 Form
     */
    public class TellersPageForm extends Form
    {
        TellersPageForm( String id )
        {
            super( id );

            add( new Label( "label1", "Welcome " + userId + " of " + getBranchId() + " branch" ) );
            add( new FtIndicatingAjaxButton( "account.deposit" )
            {
                @Override
                protected void onSubmit( AjaxRequestTarget target, Form form )
                {
                    logIt( target, "Account, Deposit Pressed" );
                }
            } );

            add( new FtIndicatingAjaxButton( "account.withdrawal" )
            {
                @Override
                protected void onSubmit( AjaxRequestTarget target, Form form )
                {
                    logIt( target, "Account, Withdrawal Pressed" );
                }
            } );

            add( new FtIndicatingAjaxButton( "account.inquiry" )
            {
                @Override
                protected void onSubmit( AjaxRequestTarget target, Form form )
                {
                    logIt( target, "Account, Inquiry Pressed" );
                }
            } );
        }
    }
}
