/*
 * This is free and unencumbered software released into the public domain.
 */
package org.rbacabac;

import org.apache.directory.fortress.core.*;
import org.apache.directory.fortress.core.SecurityException;
import org.apache.directory.fortress.core.model.User;
import org.apache.directory.fortress.realm.J2eePolicyMgr;
import org.apache.directory.fortress.web.control.SecUtils;
import org.apache.directory.fortress.core.model.Session;
import org.apache.directory.fortress.core.model.UserRole;
import org.apache.directory.fortress.web.control.FtBookmarkablePageLink;
import org.apache.directory.fortress.web.control.FtIndicatingAjaxButton;
import org.apache.directory.fortress.web.control.WicketSession;
import org.apache.log4j.Logger;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.List;
import java.util.Properties;

/**
 * Base class for RbacAbac Sample Project.
 *
 * @author Shawn McKinney
 * @version $Rev$
 */
public abstract class WicketSampleBasePage extends WebPage
{
    // Fortress spring beans injected here:
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
        // Add FtBookmarkablePageLink will show link to user if they have the permission:
        add( new FtBookmarkablePageLink( "tellerspage.link", TellersPage.class ) );
        add( new FtBookmarkablePageLink( "washerspage.link", WashersPage.class ) );
        add( new UsersForm( "usersForm" ) );
        add( new Label( "footer", "This is free and unencumbered software released into the public domain." ) );
        add( new Label( GlobalIds.INFO_FIELD ));
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

            add( new FtIndicatingAjaxButton( "branch.login" )
            {
                @Override
                protected void onSubmit(AjaxRequestTarget target, Form form)
                {
                    String branch = (String)branchField.getDefaultModelObject();
                    initializeSession( this, getUserid(), branch );
                    logIt( target, "Login to Branch: "  + branch);
                    setResponsePage( HomePage.class );
                }
            } );

            HttpServletRequest servletReq = ( HttpServletRequest ) getRequest().getContainerRequest();
            // RBAC Security Processing:
            Principal principal = servletReq.getUserPrincipal();

            // Is this a Java EE secured page && has the User successfully authenticated already?
            boolean isSecured = principal != null;
            if ( isSecured )
            {
                if ( !SecUtils.isLoggedIn( this ) )
                {
                    try
                    {
                        String szPrincipal = principal.toString();
                        // Pull the RBAC session from the realm and assert intno the Web app's session along with user's
                        SecUtils.initializeSession( this, j2eePolicyMgr, accessMgr, szPrincipal );
                    }
                    catch ( org.apache.directory.fortress.core.SecurityException se )
                    {
                        throw new RuntimeException( se );
                    }
                }
            }
        }
    }


    protected String getUserid()
    {
        String userid;
        WicketSession session = ( WicketSession ) this.getSession();
        Session ftSess = session.getSession();
        userid = ftSess.getUserId();
        return userid;
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

    protected static final Logger LOG = Logger.getLogger( WicketSampleBasePage.class.getName() );

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
            Properties props = new Properties(  );
            props.setProperty( "locale", branchId );
            User user = new User(userId);
            user.addProperties( props );
            Session session = null;
            try
            {
                session = accessMgr.createSession( user, true );
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