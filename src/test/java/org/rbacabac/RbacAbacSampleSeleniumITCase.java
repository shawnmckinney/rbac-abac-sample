package org.rbacabac;

import java.lang.String;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.junit.*;
import static org.junit.Assert.*;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

/**
 * This class uses apache selenium firefox driver to drive commander web ui
 */
public class RbacAbacSampleSeleniumITCase
{
    private WebDriver driver;
    private String baseUrl;
    private boolean acceptNextAlert = true;
    private StringBuffer verificationErrors = new StringBuffer();
    private static final Logger LOG = Logger.getLogger( RbacAbacSampleSeleniumITCase.class.getName() );
    private static final String DRIVER_SYS_PROP = "web.driver";
    private static final String USER_ID = "userId";
    private static final String PSWD_FIELD = "pswdField";
    private static final String LOGIN = "login";
    private static final String BRANCH_LOGIN = "branch.login";
    private static final String PAGE_WASHERS = "WashersPage";
    private static final String PAGE_WASHERS_LINK = "Washers Page";
    private static final String PAGE_TELLERS = "TellersPage";
    private static final String PAGE_TELLERS_LINK = "Tellers Page";
    private static final String BTN_ACCOUNT_DEPOSIT = "account.deposit";
    private static final String BTN_ACCOUNT_WITHDRAWAL = "account.withdrawal";
    private static final String BTN_ACCOUNT_INQUIRY = "account.inquiry";
    private static final String BTN_CURRENCY_SOAK = "currency.soak";
    private static final String BTN_CURRENCY_RINSE = "currency.rinse";
    private static final String BTN_CURRENCY_DRY = "currency.dry";
    private static final String BRANCH = "branch";
    private static final String CURLY = "curly";
    private static final String MOE = "moe";
    private static final String LARRY = "larry";
    private static final String PASSWORD = "password";
    private static final String NORTH = "North";
    private static final String SOUTH = "South";
    private static final String EAST = "East";
    private static final String WEST = "West";

    private enum DriverType
    {
        FIREFOX,
        CHROME
    }

    private static DriverType driverType = DriverType.FIREFOX;

    @Before
    public void setUp() throws Exception
    {
        // Use test local default:
        baseUrl = "http://localhost:8080";
        baseUrl += "/rbac-abac-sample";
        driver.manage().timeouts().implicitlyWait( 2500, TimeUnit.MILLISECONDS );
    }

    private void info(String msg)
    {
        ( ( JavascriptExecutor ) driver ).executeScript( "$(document.getElementById('infoField')).val('" + msg + "');" );
    }

    @BeforeClass
    public static void setupClass()
    {
        String szDriverType = System.getProperty( DRIVER_SYS_PROP );
        if( StringUtils.isNotEmpty( szDriverType ) && szDriverType.equalsIgnoreCase( DriverType.CHROME.toString() ))
        {
            driverType = DriverType.CHROME;
            WebDriverManager.chromedriver().setup();
        }
        else
        {
            WebDriverManager.firefoxdriver().setup();
        }
    }

    @Before
    public void setupTest()
    {
        if ( driverType.equals( DriverType.CHROME ) )
        {
            driver = new ChromeDriver();
        }
        else
        {
            driver = new FirefoxDriver( );
        }
        driver.manage().window().maximize();
    }

    @After
    public void teardown()
    {
        if (driver != null)
        {
            driver.quit();
        }
    }

    @Test
    public void testCurly() throws Exception
    {
        LOG.info( "Begin RoleSampleSeleniumITCase Test Case #1" );
        driver.get( baseUrl );

        login( CURLY, PASSWORD );
        TUtils.sleep( 1 );
        doNegativeLinkTest( PAGE_TELLERS_LINK, CURLY );
        doNegativeLinkTest( PAGE_WASHERS_LINK, CURLY );

        // Curly is Teller in East:
        doSetLocation( EAST );
        doNegativeLinkTest( PAGE_WASHERS_LINK, CURLY );
        doTellerPositiveButtonTests( PAGE_TELLERS_LINK, PAGE_TELLERS );

        // Curly is Washer in North:
        doSetLocation( NORTH );
        doNegativeLinkTest( PAGE_TELLERS_LINK, CURLY );
        doWasherPositiveButtonTests( PAGE_WASHERS_LINK, PAGE_WASHERS );

        // Curly is Washer in South:
        doSetLocation( SOUTH );
        doNegativeLinkTest( PAGE_TELLERS_LINK, CURLY );
        doWasherPositiveButtonTests( PAGE_WASHERS_LINK, PAGE_WASHERS );

        logout( CURLY );
    }


    @Test
    public void testMoe() throws Exception
    {
        LOG.info( "Begin RoleSampleSeleniumITCase Test Case #1" );
        driver.get( baseUrl );

        login( MOE, PASSWORD );
        TUtils.sleep( 1 );
        doNegativeLinkTest( PAGE_TELLERS_LINK, MOE );
        doNegativeLinkTest( PAGE_WASHERS_LINK, MOE );

        // Moe is Teller in North:
        doSetLocation( NORTH );
        doNegativeLinkTest( PAGE_WASHERS_LINK, MOE );
        doTellerPositiveButtonTests( PAGE_TELLERS_LINK, PAGE_TELLERS );

        // Moe is Washer in East:
        doSetLocation( EAST );
        doNegativeLinkTest( PAGE_TELLERS_LINK, MOE );
        doWasherPositiveButtonTests( PAGE_WASHERS_LINK, PAGE_WASHERS );

        // Moe is Washer in South:
        doSetLocation( SOUTH );
        doNegativeLinkTest( PAGE_TELLERS_LINK, MOE );
        doWasherPositiveButtonTests( PAGE_WASHERS_LINK, PAGE_WASHERS );

        logout( MOE );
    }


    @Test
    public void testLarry() throws Exception
    {
        LOG.info( "Begin RoleSampleSeleniumITCase Test Case #1" );
        driver.get( baseUrl );

        login( LARRY, PASSWORD );
        TUtils.sleep( 1 );
        doNegativeLinkTest( PAGE_TELLERS_LINK, LARRY );
        doNegativeLinkTest( PAGE_WASHERS_LINK, LARRY );

        // Larry is Teller in South:
        doSetLocation( SOUTH );
        doNegativeLinkTest( PAGE_WASHERS_LINK, LARRY );
        doTellerPositiveButtonTests( PAGE_TELLERS_LINK, PAGE_TELLERS );

        // Larry is Washer in North:
        doSetLocation( NORTH );
        doNegativeLinkTest( PAGE_TELLERS_LINK, LARRY );
        doWasherPositiveButtonTests( PAGE_WASHERS_LINK, PAGE_WASHERS );

        // Larry is Washer in East:
        doSetLocation( EAST );
        doNegativeLinkTest( PAGE_TELLERS_LINK, LARRY );
        doWasherPositiveButtonTests( PAGE_WASHERS_LINK, PAGE_WASHERS );

        logout( LARRY );
    }


    private void doSetLocation( String location )
    {
        driver.findElement( By.id( BRANCH ) ).clear();
        driver.findElement( By.id( BRANCH ) ).sendKeys( location );
        driver.findElement( By.name( BRANCH_LOGIN ) ).click();
    }


    private void doTellerPositiveButtonTests( String linkName, String pageId )
    {
        if(linkName != null)
            driver.findElement( By.linkText( linkName ) ).click();
        TUtils.sleep( 1 );
        // Click the buttons on the page
        doPositiveButtonTest(pageId, BTN_ACCOUNT_DEPOSIT, pageId + "." + BTN_ACCOUNT_DEPOSIT);
        doPositiveButtonTest(pageId, BTN_ACCOUNT_WITHDRAWAL, pageId + "." + BTN_ACCOUNT_WITHDRAWAL);
        doPositiveButtonTest(pageId, BTN_ACCOUNT_INQUIRY, pageId + "." + BTN_ACCOUNT_INQUIRY);
    }


    private void doWasherPositiveButtonTests( String linkName, String pageId )
    {
        if(linkName != null)
            driver.findElement( By.linkText( linkName ) ).click();
        TUtils.sleep( 1 );
        // Click the buttons on the page
        doPositiveButtonTest(pageId, BTN_CURRENCY_SOAK, pageId + "." + BTN_CURRENCY_SOAK);
        doPositiveButtonTest(pageId, BTN_CURRENCY_RINSE, pageId + "." + BTN_CURRENCY_RINSE);
        doPositiveButtonTest(pageId, BTN_CURRENCY_DRY, pageId + "." + BTN_CURRENCY_DRY);
    }


    private boolean processPopup(String text)
    {
        boolean textFound = false;
        try
        {
            Alert alert = driver.switchTo ().alert ();
            //alert is present
            LOG.info( "Button Pressed:" + alert.getText() );
            if(alert.getText().contains( text ))
                textFound = true;

            alert.accept();
        }
        catch ( NoAlertPresentException n)
        {
            //Alert isn't present
        }
        return textFound;
    }

    private void doPositiveButtonTest(String pageId, String buttonId, String alertText)
    {
        info("Positive button test for " + pageId + ", " + buttonId);
        try
        {
            driver.findElement( By.name( pageId + "." + buttonId ) ).click();
        }
        catch(Exception e)
        {
            LOG.error( "activateRole Exception: " + e);
        }

        //TUtils.sleep( 1 );
        //if(!processPopup(alertText))
        //    fail("Button Test Failed: " + pageId + "." + buttonId);
    }

    private void login(String userId, String password)
    {
        driver.findElement( By.id( USER_ID ) ).clear();
        driver.findElement( By.id( USER_ID ) ).sendKeys( userId );
        driver.findElement( By.id( PSWD_FIELD ) ).clear();
        driver.findElement( By.id( PSWD_FIELD ) ).sendKeys( password );
        driver.findElement( By.name( LOGIN ) ).click();
        LOG.info( "User: " + userId + " has logged ON" );
        info("Login User: " + userId);
    }

    private void logout(String userId)
    {
        info( "Logout " + userId );
        driver.findElement( By.linkText( "LOGOUT" ) ).click();
        LOG.info( "User: " + userId + " has logged OFF" );
    }

    private void doNegativeLinkTest( String linkName, String userId  )
    {
        info( "Negative link test for " + userId + " on " + linkName);

        try
        {
            if(driver.findElement( By.linkText( linkName ) ).isEnabled())
            {
                fail("Negative Link Test Failed UserId: " + userId + " Link: " + linkName);
            }
            fail("Negative Button Test Failed UserId: " + userId + " Link: " + linkName);
        }
        catch (org.openqa.selenium.NoSuchElementException e)
        {
            // pass
        }
        try
        {
            if(driver.findElement( By.linkText( linkName ) ).isEnabled())
            {
                fail("Negative Link Test Failed UserId: " + userId + " Link: " + linkName);
            }
        }
        catch (org.openqa.selenium.NoSuchElementException e)
        {
            // pass
        }
    }

    private void nextPage(WebElement table, String szTableName)
    {
        table = driver.findElement(By.id( szTableName));
        List<WebElement> allRows = table.findElements(By.tagName("a"));
        for (WebElement row : allRows)
        {
            String szText = row.getText();
            if(szText.equals( "Go to the next page" ))
                row.click();
            LOG.debug( "row text=" + row.getText() );
        }
    }

    @After
    public void tearDown() throws Exception
    {
        driver.quit();
        String verificationErrorString = verificationErrors.toString();
        if ( !"".equals( verificationErrorString ) )
        {
            fail( verificationErrorString );
        }
    }

    private boolean isElementPresent( By by )
    {
        try
        {
            driver.findElement( by );
            return true;
        }
        catch ( NoSuchElementException e )
        {
            return false;
        }
    }

    private boolean isAlertPresent()
    {
        try
        {
            driver.switchTo().alert();
            return true;
        }
        catch ( NoAlertPresentException e )
        {
            return false;
        }
    }

    private String closeAlertAndGetItsText()
    {
        try
        {
            Alert alert = driver.switchTo().alert();
            String alertText = alert.getText();
            if ( acceptNextAlert )
            {
                alert.accept();
            }
            else
            {
                alert.dismiss();
            }
            return alertText;
        }
        finally
        {
            acceptNextAlert = true;
        }
    }
}
