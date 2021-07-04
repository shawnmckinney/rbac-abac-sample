/*
 * This is free and unencumbered software released into the public domain.
 */
package org.rbacabac;

import org.apache.directory.fortress.core.*;
import org.apache.directory.fortress.core.SecurityException;
import org.apache.directory.fortress.core.model.RoleConstraint;
import org.apache.directory.fortress.core.model.User;
import org.apache.directory.fortress.realm.J2eePolicyMgr;
import org.apache.directory.fortress.web.control.SecUtils;
import org.apache.directory.fortress.core.model.Session;
import org.apache.directory.fortress.web.control.FtBookmarkablePageLink;
import org.apache.directory.fortress.web.control.WicketSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for RbacAbac Sample Project.
 *
 * @author Shawn McKinney
 * @version $Rev$
 */
public abstract class WicketSampleBasePage extends WebPage
{
    // The access management APIs:
    @SpringBean
    private AccessMgr accessMgr;
    @SpringBean
    private J2eePolicyMgr j2eePolicyMgr;

    public WicketSampleBasePage()
    {
        final Link actionLink = new Link( "logout.link" )
        {
            @Override
            public void onClick()
            {
                HttpServletRequest servletReq = ( HttpServletRequest ) getRequest().getContainerRequest();
                servletReq.getSession().invalidate();
                getSession().invalidate();
                setResponsePage( LoginPage.class );
            }
        };
        add( actionLink );
        // These are 'secured' buttons meaning user must have..
        add( new FtBookmarkablePageLink( "tellerspage.link", TellersPage.class ) );
        add( new FtBookmarkablePageLink( "washerspage.link", WashersPage.class ) );
        add( new UsersForm( "usersForm" ) );
        add( new Label( "footer", "This is free and unencumbered software released into the public domain." ) );
        add( new Label( "infoField" ));
    }

    /**
     * Page 1 Form
     */
    public class UsersForm extends Form
    {
        private TextField branchField;

        public UsersForm(String id)
        {
            super( id );
            branchField = new TextField("branch", Model.of(""));
            add(branchField);

            // Not a secured button b/c in this sample, any authorized app user may attempt branch logins:
            add( new IndicatingAjaxButton( "branch.login" )
            {
                @Override
                protected void onSubmit( AjaxRequestTarget target )
                {
                    String branch = (String)branchField.getDefaultModelObject();
                    initializeSession( this, getUserid(), branch );
                    logIt( target, "Login to Branch: "  + branch);
                    setResponsePage( HomePage.class );
                }
            } );
        }
    }

    protected String getUserid()
    {
        HttpServletRequest servletReq = ( HttpServletRequest ) getRequest().getContainerRequest();
        Principal principal = servletReq.getUserPrincipal();
        return principal.getName();
    }

    protected String getBranchId()
    {
        return (String)( WicketSession.get() ).getAttribute( "branchId");
    }

    /**
     * Used by the child pages.
     *
     * @param target for modal panel
     * @param msg    to log and display user info
     */
    protected void logIt(AjaxRequestTarget target, String msg)
    {
        info( msg );
        LOG.info( msg );
        target.appendJavaScript( ";alert('" + msg + "');" );
    }

    protected static final Logger LOG = LoggerFactory.getLogger( WicketSampleBasePage.class.getName() );

    /**
     *
     * @param component
     * @param userId
     * @param branchId
     */
    public void initializeSession( Component component, String userId, String branchId )
    {
        synchronized ( ( WicketSession ) WicketSession.get() )
        {
            LOG.info( "Session user: " + userId );
            User user = new User(userId);
            RoleConstraint constraint = new RoleConstraint();
            constraint.setKey( "locale" );
            constraint.setValue( branchId );
            List<RoleConstraint> constraints = new ArrayList();
            constraints.add( constraint );
            Session session;
            try
            {
                session = accessMgr.createSession( user, constraints, true );

            }
            catch (SecurityException se)
            {
                throw new RuntimeException( se );
            }
            // Retrieve user permissions and attach RBAC session to Wicket session:
            ( ( WicketSession ) WicketSession.get() ).setSession( session );
            ( WicketSession.get() ).setAttribute( "branchId", branchId );
            SecUtils.getPermissions( component, accessMgr );
        }
    }
}
