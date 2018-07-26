/*
 * This is free and unencumbered software released into the public domain.
 */
package org.rbacabac;

import org.apache.commons.collections.CollectionUtils;
import org.apache.directory.fortress.core.*;
import org.apache.directory.fortress.realm.*;
import org.apache.directory.fortress.web.control.SecUtils;
import org.apache.directory.fortress.web.control.SecureIndicatingAjaxButton;

import org.apache.directory.fortress.core.model.Session;
import org.apache.directory.fortress.core.model.UserRole;
import org.apache.directory.fortress.web.control.FtBookmarkablePageLink;
import org.apache.directory.fortress.web.control.FtIndicatingAjaxButton;
import org.apache.directory.fortress.web.control.WicketSession;
import org.apache.log4j.Logger;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.settings.ExceptionSettings;
import org.apache.wicket.spring.injection.annot.SpringBean;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.List;

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
    @SpringBean
    private ReviewMgr reviewMgr;

    private final String TELLER = "Role_Tellers";
    private final String WASHER = "Role_Washers";
    final String HOME_PAGE_OBJ = "org.rbacabac.HomePage";
    final String SWITCH_TELLER_OP = "switchToTeller";
    final String SWITCH_WASHER_OP = "switchToWasher";
    final String SWITCH_ROLES_OP = "switchRoles";

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
                    // perms:
                    SecUtils.initializeSession( this, j2eePolicyMgr, accessMgr, szPrincipal );

                    //accessMgr.createSession(  )
                }
                catch ( org.apache.directory.fortress.core.SecurityException se )
                {
                    throw new RuntimeException( se );
                }
            }
        }
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
        private List<UserRole> inactiveRoles;
        private List<UserRole> activeRoles;

        public UsersForm(String id)
        {
            super( id );
            add ( new TextField( "customer" ) );

            add( new FtIndicatingAjaxButton( "branch.login" )
            {
                @Override
                protected void onSubmit(AjaxRequestTarget target, Form form)
                {
                    logIt( target, "Branch, Login Pressed" );
                }
            } );
            
            loadActivatedRoleSets();
            addRoleActivationComboBoxesAndButtons();
        }

        /**
         * This loads the set of user's activated roles into a local page variable.  It is used for deactivate combo
         * box.
         */
        private void loadActivatedRoleSets()
        {
            Session session = SecUtils.getSession( this );
            if ( session != null )
            {
                LOG.info( "get assigned roles for user: " + session.getUserId() );
                try
                {
                    inactiveRoles = reviewMgr.assignedRoles( session.getUser() );
                    // remove inactiveRoles already activated:
                    for ( UserRole activatedRole : session.getRoles() )
                    {
                        inactiveRoles.remove( activatedRole );
                    }
                    LOG.info( "user: " + session.getUserId() + " inactiveRoles for activate list: " + inactiveRoles );
                    activeRoles = session.getRoles();
                }
                catch ( org.apache.directory.fortress.core.SecurityException se )
                {
                    String error = "SecurityException getting assigned inactiveRoles for user: " + session.getUserId();
                    LOG.error( error );
                }
            }
        }

        private void addRoleActivationComboBoxesAndButtons()
        {
            add( new SecureIndicatingAjaxButton( this, GlobalIds.BTN_SWITCH_WASHER, HOME_PAGE_OBJ, SWITCH_WASHER_OP )
            {
                private static final long serialVersionUID = 1L;

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form)
                {
                    getApplication().getExceptionSettings().setAjaxErrorHandlingStrategy( ExceptionSettings
                        .AjaxErrorStrategy.REDIRECT_TO_ERROR_PAGE );
                    if ( checkAccess( HOME_PAGE_OBJ, SWITCH_ROLES_OP ) )
                    {
                        switchToWasher();
                        logIt( target, "Switch To Washer Successful" );
                        setResponsePage( HomePage.class );
                    }
                    else
                    {
                        String msg = "You not authorized switch to Washer";
                        PageParameters parameters = new PageParameters();
                        parameters.add( "errorValue", msg );
                        setResponsePage( AuthZErrorPage.class, parameters );
                    }
                }

                @Override
                protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
                {
                    AjaxCallListener ajaxCallListener = new AjaxCallListener()
                    {
                        @Override
                        public CharSequence getFailureHandler(Component component)
                        {
                            String szRelocation = getLocationReplacement( ( HttpServletRequest ) getRequest()
                                .getContainerRequest() );
                            LOG.info( "HomePage.switchToWasher Failure Handler, relocation string = " + szRelocation );
                            return szRelocation;
                        }
                    };
                    attributes.getAjaxCallListeners().add( ajaxCallListener );
                }
            } );

            add( new SecureIndicatingAjaxButton( this, GlobalIds.BTN_SWITCH_TELLER, HOME_PAGE_OBJ, SWITCH_TELLER_OP )
            {
                private static final long serialVersionUID = 1L;

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form)
                {
                    getApplication().getExceptionSettings().setAjaxErrorHandlingStrategy( ExceptionSettings
                        .AjaxErrorStrategy.REDIRECT_TO_ERROR_PAGE );
                    if ( checkAccess( HOME_PAGE_OBJ, SWITCH_ROLES_OP ) )
                    {
                        switchToTeller();
                        logIt( target, "Switch To Teller Successful" );
                        setResponsePage( HomePage.class );
                    }
                    else
                    {
                        String msg = "You are not authorized switch to Teller";
                        PageParameters parameters = new PageParameters();
                        parameters.add( "errorValue", msg );
                        setResponsePage( AuthZErrorPage.class, parameters );
                    }
                }

                @Override
                protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
                {
                    AjaxCallListener ajaxCallListener = new AjaxCallListener()
                    {
                        @Override
                        public CharSequence getFailureHandler(Component component)
                        {
                            String szRelocation = getLocationReplacement( ( HttpServletRequest ) getRequest()
                                .getContainerRequest() );
                            LOG.info( "HomePage.switchToTeller Failure Handler, relocation string = " + szRelocation );
                            return szRelocation;
                        }
                    };
                    attributes.getAjaxCallListeners().add( ajaxCallListener );
                }
            } );

            Label inactivatedRoleString = new Label( "inactivatedRoleString", new PropertyModel<String>( this,
                "inactivatedRoleString" ) );
            add( inactivatedRoleString );
        }

        private void switchToTeller()
        {
            try
            {
                WicketSession session = ( WicketSession ) this.getSession();
                accessMgr.dropActiveRole( session.getSession(), new UserRole( session.getSession().getUserId(), TELLER
                ) );
                accessMgr.addActiveRole( session.getSession(), new UserRole( session.getSession().getUserId(), WASHER
                ) );
                SecUtils.getPermissions( this, accessMgr );
            }
            catch ( org.apache.directory.fortress.core.SecurityException se )
            {
                throw new RuntimeException( se );
            }
        }

        private void switchToWasher()
        {
            try
            {
                WicketSession session = ( WicketSession ) this.getSession();
                accessMgr.dropActiveRole( session.getSession(), new UserRole( session.getSession().getUserId(), WASHER ) );
                accessMgr.addActiveRole( session.getSession(), new UserRole( session.getSession().getUserId(), TELLER
                ) );
                SecUtils.getPermissions( this, accessMgr );
            }
            catch ( org.apache.directory.fortress.core.SecurityException se )
            {
                throw new RuntimeException( se );
            }
        }

        /**
         * Build a comma delimited String containing inactivated roles to be displayed in page label.
         *
         * @return String containing comma delimited inactivated roles
         */
        public String getInactivatedRoleString()
        {
            String szRoleStr = "";
            if ( CollectionUtils.isNotEmpty( inactiveRoles ) )
            {
                int ctr = 0;
                for ( UserRole role : inactiveRoles )
                {
                    if ( ctr++ > 0 )
                    {
                        szRoleStr += ", ";
                    }
                    szRoleStr += role.getName();
                }
            }
            return szRoleStr;
        }

        private String getLocationReplacement(HttpServletRequest servletRequest)
        {
            return "window.location.replace(\"" + servletRequest.getContextPath() + "\");";
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
}