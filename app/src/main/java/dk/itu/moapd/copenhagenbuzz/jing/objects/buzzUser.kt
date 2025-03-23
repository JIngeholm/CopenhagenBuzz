package dk.itu.moapd.copenhagenbuzz.jing.objects

data class buzzUser(
    var username: String = "",
    var email: String = "",
    var profilePicture: String = "",
    var uid: String = ""

){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is buzzUser) return false
        return this.uid == other.uid
    }

    override fun hashCode(): Int {
        return uid.hashCode()
    }
}