package rocks.crimp.crimp;

import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.MediumTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import okhttp3.mockwebserver.MockWebServer;
import rocks.crimp.crimp.network.CrimpWS;
import rocks.crimp.crimp.network.CrimpWsImpl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
@RunWith(AndroidJUnit4.class)
@MediumTest
public class WebServiceTest {
    private CrimpWS mCrimpWSImpl;

    @Before
    public void createCrimpWS() {
        mCrimpWSImpl = new CrimpWsImpl();
    }

    @Test
    public void testgetBaseUrl() {
        assertThat(mCrimpWSImpl.getBaseUrl(), is("http://dev.crimp.rocks/"));
    }

    @Test
    public void testGetCategories(){
        // Create a MockWebServer. These are lean enough that you can create a new
        // instance for every unit test.
        MockWebServer server = new MockWebServer();

    }
}
