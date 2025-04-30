package nl.parlementairemonitor.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


class SyncFeedUtilTest {

    val XML = """
<commissieZetelVastPersoon xmlns:tk="http://www.tweedekamer.nl/xsd/tkData/v1-0" xmlns="http://www.tweedekamer.nl/xsd/tkData/v1-0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" id="c8db0181-90e3-4df3-a150-4c004584b160" tk:bijgewerkt="2023-08-29T11:09:59Z" tk:verwijderd="false">
<commissieZetel ref="b668259b-ff96-49b5-9796-9dd303625aaf"/>
<persoon ref="16ed90a7-8b21-43a5-916c-76f07231325a"/>
<functie>Lid</functie>
<van>1996-05-07</van>
<totEnMet>2006-11-29</totEnMet>
</commissieZetelVastPersoon>
"""

    @Test
    fun testConvertTypeToMap() {
        val content = SyncFeedUtil.unmarshalJAXBElement(XML.byteInputStream())
        val map = SyncFeedUtil.convertToMap(content)
        assertEquals("c8db0181-90e3-4df3-a150-4c004584b160", map["_id"])
        assertEquals("16ed90a7-8b21-43a5-916c-76f07231325a", map["persoon"])
        assertEquals("Lid", map["functie"])
    }

}