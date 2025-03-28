package dk.itu.moapd.copenhagenbuzz.jing.objects

data class EventLocation(
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var address: String = ""
) {
    constructor() : this(0.0, 0.0, "")
}