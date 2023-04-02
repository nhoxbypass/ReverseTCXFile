import jdk.internal.org.xml.sax.SAXException
import org.w3c.dom.Node
import java.io.File
import java.io.IOException
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

fun main(args: Array<String>) {
    try {
        // read file
        val inputStream = File("activity.tcx").inputStream()
        val builderFactory = DocumentBuilderFactory.newInstance()
        val docBuilder = builderFactory.newDocumentBuilder()
        val rootDocument = docBuilder.parse(inputStream)

        // swap node
        val trackPoints = rootDocument.getElementsByTagName("Trackpoint")
        val length = trackPoints.length
        for (i in 0 until (length / 2)) {
            val headPoint = trackPoints.item(i)
            val tailPoint = trackPoints.item(length - 1 - i)
            swapPoints(headPoint, tailPoint)
        }

        // save to new file
        val outputStream = File("activity_res.tcx").outputStream()
        val transformerFactory = TransformerFactory.newInstance()
        val transformer = transformerFactory.newTransformer()
        val dSource = DOMSource(rootDocument)
        val result = StreamResult(outputStream)
        transformer.transform(dSource, result)
    } catch (e: IOException) {
        e.printStackTrace()
    } catch (e: ParserConfigurationException) {
        e.printStackTrace()
    } catch (e: SAXException) {
        e.printStackTrace()
    }
}

fun swapPoints(headPoint: Node?, tailPoint: Node?) {
    if (headPoint == null || tailPoint == null) return

    val headChildList = headPoint.childNodes
    val tailChildList = tailPoint.childNodes

    for (i in 0 until headChildList.length) {
        val headChild = headChildList.item(i)
        val tailChild = tailChildList.item(i)

        if (headChild.nodeType == Node.TEXT_NODE || tailChild.nodeType == Node.TEXT_NODE) continue
        if (headChild.nodeName == "Time"
            || headChild.nodeName == "DistanceMeters"
            || headChild.nodeName == "Extensions"
            || headChild.nodeName == "HeartRateBpm"
        ) continue

        if (headChild.nodeName == "Position") {
            val headLat = headChild.childNodes.item(1)
            val headLong = headChild.childNodes.item(3)
            val tailLat = tailChild.childNodes.item(1)
            val tailLong = tailChild.childNodes.item(3)
            headChild.removeChild(headLat)
            headChild.removeChild(headLong)
            tailChild.removeChild(tailLat)
            tailChild.removeChild(tailLong)
            headChild.appendChild(tailLat)
            headChild.appendChild(tailLong)
            tailChild.appendChild(headLat)
            tailChild.appendChild(headLong)
        } else {
            headPoint.removeChild(headChild)
            tailPoint.removeChild(tailChild)
            headPoint.appendChild(tailChild)
            tailPoint.appendChild(headChild)
        }
    }
}