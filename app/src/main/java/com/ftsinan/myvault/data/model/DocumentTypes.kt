package com.ftsinan.myvault.data.model

/**
 * Suggested document types per category, tailored for India.
 * Users can always type a custom document type instead.
 */
object DocumentTypes {

    val byCategory: Map<Category, List<String>> = mapOf(
        Category.IDENTITY to listOf(
            "Aadhaar Card",
            "PAN Card",
            "Passport",
            "Voter ID (EPIC)",
            "Driving Licence",
            "Ration Card",
            "DigiLocker ID",
            "Other ID Proof"
        ),
        Category.EDUCATION to listOf(
            "SSLC Certificate",
            "Plus Two / HSC Certificate",
            "Degree Certificate",
            "PG Certificate",
            "Mark List",
            "Transfer Certificate (TC)",
            "Conduct Certificate",
            "Course Completion Certificate",
            "Other Education Document"
        ),
        Category.CERTIFICATES to listOf(
            "Birth Certificate",
            "Marriage Certificate",
            "Death Certificate",
            "Income Certificate",
            "Caste / Community Certificate",
            "Nativity Certificate",
            "Residence Certificate",
            "Legal Heir Certificate",
            "Other Certificate"
        ),
        Category.VEHICLE to listOf(
            "RC Book",
            "Vehicle Insurance",
            "Pollution Certificate (PUC)",
            "Road Tax Receipt",
            "Vehicle Invoice",
            "Other Vehicle Document"
        ),
        Category.BANKING to listOf(
            "Bank Passbook",
            "Debit Card",
            "Credit Card",
            "Cancelled Cheque",
            "Fixed Deposit Receipt",
            "Loan Document",
            "Insurance Policy (Life)",
            "Other Banking Document"
        ),
        Category.HEALTH to listOf(
            "Health Insurance Card",
            "ABHA / Ayushman Card",
            "Vaccination Certificate",
            "Medical Report",
            "Prescription",
            "Other Health Document"
        ),
        Category.PROPERTY to listOf(
            "Land Deed / Title Deed",
            "Land Tax Receipt",
            "Property Tax Receipt",
            "Electricity Bill",
            "Water Bill",
            "Rental Agreement",
            "Possession Certificate",
            "Other Property Document"
        ),
        Category.OTHER to listOf(
            "Other Document"
        )
    )

    fun suggestionsFor(category: Category): List<String> =
        byCategory[category].orEmpty()

    /**
     * Document types that normally have an expiry date worth tracking.
     */
    private val expiringTypes = setOf(
        "Passport",
        "Driving Licence",
        "Vehicle Insurance",
        "Pollution Certificate (PUC)",
        "Income Certificate",
        "Insurance Policy (Life)",
        "Health Insurance Card",
        "Rental Agreement"
    )

    fun usuallyHasExpiry(docType: String): Boolean = docType in expiringTypes
}
