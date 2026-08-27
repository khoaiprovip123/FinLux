package com.finlux.app.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finlux.app.R
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.domain.model.WalletType

enum class InstitutionCategory(val label: String) {
    ALL("Tất cả"),
    BANK("Ngân hàng"),
    EWALLET("Ví điện tử"),
    CASH_SAVINGS("Tiền mặt & Tiết kiệm"),
    INVESTMENT("Đầu tư & Thẻ"),
}

data class FinancialInstitution(
    val id: String,
    val shortName: String,
    val fullName: String,
    val code: String,
    val type: WalletType,
    val category: InstitutionCategory,
    val colorHex: String = "#3478F6",
    val iconRes: Int? = null,
    val keywords: List<String> = emptyList(),
)

private val CuratedFinancialInstitutionMetadata = listOf(
    // ----------------------------------------------------
    // 1. TIỀN MẶT & TIẾT KIỆM
    // ----------------------------------------------------
    FinancialInstitution(
        id = "cash",
        shortName = "Tiền mặt",
        fullName = "Ví tiền mặt chi tiêu hàng ngày",
        code = "CASH",
        type = WalletType.CASH,
        category = InstitutionCategory.CASH_SAVINGS,
        colorHex = "#20B982",
        iconRes = R.drawable.ic_wallet_cash,
        keywords = listOf("tiền mặt", "cash", "tien mat", "vi tien"),
    ),
    FinancialInstitution(
        id = "savings",
        shortName = "Sổ tiết kiệm",
        fullName = "Tiền gửi tiết kiệm / Heo đất",
        code = "SAVE",
        type = WalletType.INVESTMENT,
        category = InstitutionCategory.CASH_SAVINGS,
        colorHex = "#F2A63B",
        iconRes = R.drawable.ic_wallet_savings,
        keywords = listOf("tiết kiệm", "tiet kiem", "heo đất", "saving", "savings"),
    ),

    // ----------------------------------------------------
    // 2. VÍ ĐIỆN TỬ (E-WALLETS)
    // ----------------------------------------------------
    FinancialInstitution(
        id = "momo",
        shortName = "MoMo",
        fullName = "Ví điện tử MoMo",
        code = "MOMO",
        type = WalletType.EWALLET,
        category = InstitutionCategory.EWALLET,
        colorHex = "#A50064",
        iconRes = R.drawable.ic_ewallet_momo,
        keywords = listOf("momo", "ví momo", "vi momo"),
    ),
    FinancialInstitution(
        id = "zalopay",
        shortName = "ZaloPay",
        fullName = "Ví điện tử ZaloPay",
        code = "ZALOPAY",
        type = WalletType.EWALLET,
        category = InstitutionCategory.EWALLET,
        colorHex = "#0068FF",
        iconRes = R.drawable.ic_ewallet_zalopay,
        keywords = listOf("zalopay", "zalo pay", "zalo", "ví zalopay"),
    ),
    FinancialInstitution(
        id = "viettelmoney",
        shortName = "Viettel Money",
        fullName = "Viettel Money (ViettelPay)",
        code = "VTMONEY",
        type = WalletType.EWALLET,
        category = InstitutionCategory.EWALLET,
        colorHex = "#EE0033",
        iconRes = R.drawable.ic_ewallet_viettelmoney,
        keywords = listOf("viettel money", "viettel pay", "viettelpay", "viettel"),
    ),
    FinancialInstitution(
        id = "vnpay",
        shortName = "VNPay",
        fullName = "Ví điện tử VNPay",
        code = "VNPAY",
        type = WalletType.EWALLET,
        category = InstitutionCategory.EWALLET,
        colorHex = "#005BAA",
        iconRes = R.drawable.ic_ewallet_vnpay,
        keywords = listOf("vnpay", "vn pay", "vnpay-qr"),
    ),
    FinancialInstitution(
        id = "shopeepay",
        shortName = "ShopeePay",
        fullName = "Ví điện tử ShopeePay (AirPay)",
        code = "SHOPEEPAY",
        type = WalletType.EWALLET,
        category = InstitutionCategory.EWALLET,
        colorHex = "#EE4D2D",
        iconRes = R.drawable.ic_ewallet_shopeepay,
        keywords = listOf("shopeepay", "shopee pay", "shopee", "airpay"),
    ),
    FinancialInstitution(
        id = "payoo",
        shortName = "Payoo",
        fullName = "Ví điện tử Payoo",
        code = "PAYOO",
        type = WalletType.EWALLET,
        category = InstitutionCategory.EWALLET,
        colorHex = "#1757A6",
        iconRes = R.drawable.ic_ewallet_payoo,
        keywords = listOf("payoo", "ví payoo", "vietunion"),
    ),
    FinancialInstitution(
        id = "9pay",
        shortName = "9Pay",
        fullName = "Ví điện tử 9Pay",
        code = "9PAY",
        type = WalletType.EWALLET,
        category = InstitutionCategory.EWALLET,
        colorHex = "#E31D2D",
        iconRes = R.drawable.ic_ewallet_9pay,
        keywords = listOf("9pay", "ninepay", "ví 9pay"),
    ),
    FinancialInstitution(
        id = "foxpay",
        shortName = "Foxpay",
        fullName = "Ví điện tử Foxpay",
        code = "FOXPAY",
        type = WalletType.EWALLET,
        category = InstitutionCategory.EWALLET,
        colorHex = "#ED1C24",
        iconRes = R.drawable.ic_ewallet_foxpay,
        keywords = listOf("foxpay", "fox pay", "fpt"),
    ),
    FinancialInstitution(
        id = "vtcpay",
        shortName = "VTC Pay",
        fullName = "Ví điện tử VTC Pay",
        code = "VTCPAY",
        type = WalletType.EWALLET,
        category = InstitutionCategory.EWALLET,
        colorHex = "#00ADEF",
        iconRes = R.drawable.ic_ewallet_vtcpay,
        keywords = listOf("vtcpay", "vtc pay", "ví vtc"),
    ),
    FinancialInstitution(
        id = "applepay",
        shortName = "Apple Pay",
        fullName = "Ví Apple Wallet / Apple Pay",
        code = "APPLEPAY",
        type = WalletType.EWALLET,
        category = InstitutionCategory.EWALLET,
        colorHex = "#1C1C1E",
        iconRes = R.drawable.ic_apple,
        keywords = listOf("apple pay", "apple wallet", "apple", "applepay"),
    ),
    FinancialInstitution(
        id = "paypal",
        shortName = "PayPal",
        fullName = "Ví thanh toán quốc tế PayPal",
        code = "PAYPAL",
        type = WalletType.EWALLET,
        category = InstitutionCategory.EWALLET,
        colorHex = "#003087",
        iconRes = R.drawable.ic_ewallet_paypal,
        keywords = listOf("paypal", "pay pal"),
    ),

    // ----------------------------------------------------
    // 3. NGÂN HÀNG (VIETNAMESE BANKS)
    // ----------------------------------------------------
    FinancialInstitution(
        id = "vcb",
        shortName = "Vietcombank",
        fullName = "Ngân hàng Ngoại thương Việt Nam",
        code = "VCB",
        type = WalletType.BANK,
        category = InstitutionCategory.BANK,
        colorHex = "#005C2B",
        iconRes = R.drawable.ic_bank_vietcombank,
        keywords = listOf("vietcombank", "vcb", "ngoại thương"),
    ),
    FinancialInstitution(
        id = "tcb",
        shortName = "Techcombank",
        fullName = "Ngân hàng Kỹ thương Việt Nam",
        code = "TCB",
        type = WalletType.BANK,
        category = InstitutionCategory.BANK,
        colorHex = "#E51A2E",
        iconRes = R.drawable.ic_bank_techcombank,
        keywords = listOf("techcombank", "tcb", "kỹ thương", "techcom"),
    ),
    FinancialInstitution(
        id = "mbbank",
        shortName = "MB Bank",
        fullName = "Ngân hàng Quân Đội",
        code = "MB",
        type = WalletType.BANK,
        category = InstitutionCategory.BANK,
        colorHex = "#1240AB",
        iconRes = R.drawable.ic_bank_mbbank,
        keywords = listOf("mb bank", "mbbank", "mb", "quân đội", "quan doi"),
    ),
    FinancialInstitution(
        id = "acb",
        shortName = "ACB",
        fullName = "Ngân hàng Á Châu",
        code = "ACB",
        type = WalletType.BANK,
        category = InstitutionCategory.BANK,
        colorHex = "#0055A5",
        iconRes = R.drawable.ic_bank_acb,
        keywords = listOf("acb", "á châu", "a chau"),
    ),
    FinancialInstitution(
        id = "vpbank",
        shortName = "VPBank",
        fullName = "Ngân hàng Việt Nam Thịnh Vượng",
        code = "VPB",
        type = WalletType.BANK,
        category = InstitutionCategory.BANK,
        colorHex = "#009140",
        iconRes = R.drawable.ic_bank_vpbank,
        keywords = listOf("vpbank", "vpb", "việt nam thịnh vượng", "thinh vuong"),
    ),
    FinancialInstitution(
        id = "bidv",
        shortName = "BIDV",
        fullName = "Ngân hàng Đầu tư và Phát triển VN",
        code = "BIDV",
        type = WalletType.BANK,
        category = InstitutionCategory.BANK,
        colorHex = "#005F6E",
        iconRes = R.drawable.ic_bank_bidv,
        keywords = listOf("bidv", "đầu tư phát triển"),
    ),
    FinancialInstitution(
        id = "vietinbank",
        shortName = "VietinBank",
        fullName = "Ngân hàng Công Thương Việt Nam",
        code = "CTG",
        type = WalletType.BANK,
        category = InstitutionCategory.BANK,
        colorHex = "#005696",
        iconRes = R.drawable.ic_bank_vietinbank,
        keywords = listOf("vietinbank", "ctg", "công thương", "vietin"),
    ),
    FinancialInstitution(
        id = "tpbank",
        shortName = "TPBank",
        fullName = "Ngân hàng Tiên Phong",
        code = "TPB",
        type = WalletType.BANK,
        category = InstitutionCategory.BANK,
        colorHex = "#5A1E82",
        iconRes = R.drawable.ic_bank_tpbank,
        keywords = listOf("tpbank", "tpb", "tiên phong", "tien phong"),
    ),
    FinancialInstitution(
        id = "hdbank",
        shortName = "HDBank",
        fullName = "Ngân hàng Phát triển TP.HCM",
        code = "HDB",
        type = WalletType.BANK,
        category = InstitutionCategory.BANK,
        colorHex = "#DE001A",
        keywords = listOf("hdbank", "hdb", "phát triển tp hcm"),
    ),
    FinancialInstitution(
        id = "sacombank",
        shortName = "Sacombank",
        fullName = "Ngân hàng Sài Gòn Thương Tín",
        code = "STB",
        type = WalletType.BANK,
        category = InstitutionCategory.BANK,
        colorHex = "#004B87",
        keywords = listOf("sacombank", "stb", "sài gòn thương tín", "sacom"),
    ),
    FinancialInstitution(
        id = "vib",
        shortName = "VIB",
        fullName = "Ngân hàng Quốc Tế",
        code = "VIB",
        type = WalletType.BANK,
        category = InstitutionCategory.BANK,
        colorHex = "#005BAA",
        keywords = listOf("vib", "quốc tế", "quoc te"),
    ),
    FinancialInstitution(
        id = "shb",
        shortName = "SHB",
        fullName = "Ngân hàng Sài Gòn - Hà Nội",
        code = "SHB",
        type = WalletType.BANK,
        category = InstitutionCategory.BANK,
        colorHex = "#F37021",
        keywords = listOf("shb", "sài gòn hà nội"),
    ),
    FinancialInstitution(
        id = "msb",
        shortName = "MSB",
        fullName = "Ngân hàng Hàng Hải Việt Nam",
        code = "MSB",
        type = WalletType.BANK,
        category = InstitutionCategory.BANK,
        colorHex = "#EA1D25",
        keywords = listOf("msb", "hàng hải", "maritime bank"),
    ),
    FinancialInstitution(
        id = "ocb",
        shortName = "OCB",
        fullName = "Ngân hàng Phương Đông",
        code = "OCB",
        type = WalletType.BANK,
        category = InstitutionCategory.BANK,
        colorHex = "#008643",
        keywords = listOf("ocb", "phương đông", "phuong dong"),
    ),
    FinancialInstitution(
        id = "seabank",
        shortName = "SeABank",
        fullName = "Ngân hàng Đông Nam Á",
        code = "SSB",
        type = WalletType.BANK,
        category = InstitutionCategory.BANK,
        colorHex = "#C8102E",
        keywords = listOf("seabank", "ssb", "đông nam á"),
    ),
    FinancialInstitution(
        id = "lpbank",
        shortName = "LPBank",
        fullName = "Ngân hàng Bưu điện Liên Việt",
        code = "LPB",
        type = WalletType.BANK,
        category = InstitutionCategory.BANK,
        colorHex = "#F39200",
        keywords = listOf("lpbank", "lpb", "lienvietpostbank", "liên việt", "bưu điện liên việt"),
    ),
    FinancialInstitution(
        id = "timo",
        shortName = "Timo",
        fullName = "Ngân hàng số Timo by BVBank",
        code = "TIMO",
        type = WalletType.BANK,
        category = InstitutionCategory.BANK,
        colorHex = "#6C28FE",
        keywords = listOf("timo", "ngân hàng số timo"),
    ),
    FinancialInstitution(
        id = "cake",
        shortName = "Cake by VPBank",
        fullName = "Ngân hàng số Cake",
        code = "CAKE",
        type = WalletType.BANK,
        category = InstitutionCategory.BANK,
        colorHex = "#FF007A",
        keywords = listOf("cake", "cake by vpbank"),
    ),
    FinancialInstitution(
        id = "tnex",
        shortName = "TNEX",
        fullName = "Ngân hàng số TNEX by MSB",
        code = "TNEX",
        type = WalletType.BANK,
        category = InstitutionCategory.BANK,
        colorHex = "#00A59B",
        keywords = listOf("tnex", "ngân hàng số tnex"),
    ),
    FinancialInstitution(
        id = "bvbank",
        shortName = "BVBank",
        fullName = "Ngân hàng Bản Việt",
        code = "BVB",
        type = WalletType.BANK,
        category = InstitutionCategory.BANK,
        colorHex = "#004D95",
        keywords = listOf("bvbank", "bvb", "bản việt", "vietcapital"),
    ),
    FinancialInstitution(
        id = "abbank",
        shortName = "ABBank",
        fullName = "Ngân hàng An Bình",
        code = "ABB",
        type = WalletType.BANK,
        category = InstitutionCategory.BANK,
        colorHex = "#00A499",
        keywords = listOf("abbank", "abb", "an bình"),
    ),
    FinancialInstitution(
        id = "shinhan",
        shortName = "Shinhan Bank",
        fullName = "Ngân hàng Shinhan Việt Nam",
        code = "SHINHAN",
        type = WalletType.BANK,
        category = InstitutionCategory.BANK,
        colorHex = "#0046FF",
        keywords = listOf("shinhan", "shinhan bank"),
    ),
    FinancialInstitution(
        id = "hsbc",
        shortName = "HSBC",
        fullName = "Ngân hàng HSBC Việt Nam",
        code = "HSBC",
        type = WalletType.BANK,
        category = InstitutionCategory.BANK,
        colorHex = "#DB0011",
        keywords = listOf("hsbc", "hsbc bank"),
    ),
    FinancialInstitution(
        id = "scb_standard",
        shortName = "Standard Chartered",
        fullName = "Ngân hàng Standard Chartered",
        code = "SCB_GLOBAL",
        type = WalletType.BANK,
        category = InstitutionCategory.BANK,
        colorHex = "#00843D",
        keywords = listOf("standard chartered", "standard"),
    ),

    // ----------------------------------------------------
    // 4. ĐẦU TƯ & THẺ TÍN DỤNG
    // ----------------------------------------------------
    FinancialInstitution(
        id = "credit_card",
        shortName = "Thẻ tín dụng",
        fullName = "Thẻ tín dụng (Credit Card)",
        code = "CREDIT",
        type = WalletType.CARD,
        category = InstitutionCategory.INVESTMENT,
        colorHex = "#3478F6",
        keywords = listOf("thẻ tín dụng", "the tin dung", "credit", "credit card", "visa", "mastercard"),
    ),
    FinancialInstitution(
        id = "stock",
        shortName = "Chứng khoán",
        fullName = "Tài khoản đầu tư Chứng khoán (VPS, SSI, TCBS...)",
        code = "STOCK",
        type = WalletType.INVESTMENT,
        category = InstitutionCategory.INVESTMENT,
        colorHex = "#7758F6",
        keywords = listOf("chứng khoán", "chung khoan", "cổ phiếu", "co phieu", "vps", "ssi", "tcbs", "vndirect"),
    ),
    FinancialInstitution(
        id = "crypto",
        shortName = "Tiền điện tử / Crypto",
        fullName = "Ví Crypto (Binance, Trust Wallet, Bybit...)",
        code = "CRYPTO",
        type = WalletType.INVESTMENT,
        category = InstitutionCategory.INVESTMENT,
        colorHex = "#F0B90B",
        keywords = listOf("crypto", "binance", "bitcoin", "tiền mã hóa", "usdt"),
    ),
)

/**
 * Danh mục dùng chung cho toàn ứng dụng: dữ liệu VietQR hiện hành được ưu tiên,
 * metadata thủ công chỉ bổ sung từ khóa/màu thương hiệu và các loại ví ngoài VietQR.
 */
val VietnameseFinancialInstitutions: List<FinancialInstitution> = buildList {
    val vietQrAliases = VietQrFinancialInstitutions
        .flatMap { listOf(it.code.lowercase(), it.shortName.lowercase()) }
        .toSet()

    addAll(
        CuratedFinancialInstitutionMetadata.filter { institution ->
            institution.category != InstitutionCategory.BANK &&
                institution.category != InstitutionCategory.INVESTMENT &&
                institution.code.lowercase() !in vietQrAliases &&
                institution.shortName.lowercase() !in vietQrAliases
        }
    )

    addAll(
        VietQrFinancialInstitutions.map { vietQrInstitution ->
            val curated = CuratedFinancialInstitutionMetadata.firstOrNull { candidate ->
                candidate.code.equals(vietQrInstitution.code, ignoreCase = true) ||
                    candidate.shortName.equals(vietQrInstitution.shortName, ignoreCase = true)
            }
            if (curated == null) {
                vietQrInstitution
            } else {
                vietQrInstitution.copy(
                    colorHex = curated.colorHex,
                    keywords = (vietQrInstitution.keywords + curated.keywords).distinct(),
                )
            }
        }
    )

    addAll(
        CuratedFinancialInstitutionMetadata.filter {
            it.category == InstitutionCategory.INVESTMENT
        }
    )
}

/**
 * Tìm ngân hàng hoặc ví điện tử dựa theo tên ví người dùng đặt
 */
fun findInstitutionForWallet(walletName: String): FinancialInstitution? {
    val clean = walletName.trim().lowercase()
    if (clean.isBlank()) return null

    val exactMatch = VietnameseFinancialInstitutions.firstOrNull { inst ->
        clean == inst.shortName.lowercase() ||
        clean == inst.code.lowercase() ||
        inst.keywords.any { clean == it.lowercase() }
    }
    if (exactMatch != null) return exactMatch

    // Ưu tiên alias dài nhất để "Techcombank" không bị nhận nhầm thành MB
    // chỉ vì chuỗi tên có chứa hai ký tự "mb".
    return VietnameseFinancialInstitutions
        .mapNotNull { institution ->
            val bestAliasLength = (
                institution.keywords + institution.shortName + institution.code
            )
                .asSequence()
                .map { it.lowercase() }
                .filter { alias -> clean.contains(alias) || alias.contains(clean) }
                .maxOfOrNull { it.length }
            bestAliasLength?.let { institution to it }
        }
        .maxByOrNull { (_, aliasLength) -> aliasLength }
        ?.first
}

/**
 * Hiển thị Logo thương hiệu Ngân hàng / Ví điện tử hoặc biểu tượng Monogram chuyên nghiệp
 */
@Composable
fun FinancialInstitutionLogo(
    institution: FinancialInstitution?,
    walletType: WalletType = WalletType.BANK,
    customColorHex: String? = null,
    size: Dp = 40.dp,
    shape: androidx.compose.ui.graphics.Shape = CircleShape,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current
    val bgHex = customColorHex ?: institution?.colorHex ?: "#3478F6"
    val bgColor = colorFromHex(bgHex)
    val hasBrandLogo = institution?.iconRes != null
    val backgroundModifier = if (hasBrandLogo) {
        Modifier.background(tokens.brandLogoSurface)
    } else {
        Modifier.background(
            Brush.linearGradient(
                colors = listOf(
                    bgColor.copy(alpha = 0.95f),
                    bgColor.copy(alpha = 0.75f),
                )
            )
        )
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .then(backgroundModifier)
            .border(
                width = 1.dp,
                color = if (hasBrandLogo) tokens.brandLogoBorder else tokens.border,
                shape = shape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (institution?.iconRes != null) {
            Icon(
                painter = painterResource(id = institution.iconRes),
                contentDescription = institution.shortName,
                tint = Color.Unspecified,
                modifier = Modifier.size(size * 0.75f),
            )
        } else if (institution != null) {
            // Hiển thị Monogram ngắn gọn, sắc nét
            val monogram = institution.code.take(3).uppercase()
            Text(
                text = monogram,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Black,
                fontSize = (size.value * 0.32f).sp,
                letterSpacing = (-0.5).sp,
            )
        } else {
            // Biểu tượng loại ví mặc định
            Icon(
                imageVector = walletIcon(walletType),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(size * 0.55f),
            )
        }
    }
}

/**
 * Component chọn nhanh Ngân hàng & Ví điện tử tích hợp cho Form tạo / sửa ví
 */
@Composable
fun InstitutionSelectorSection(
    selectedInstitution: FinancialInstitution?,
    onSelectInstitution: (FinancialInstitution) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current
    var selectedCategory by remember { mutableStateOf(InstitutionCategory.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var showFullListModal by remember { mutableStateOf(false) }

    val filteredInstitutions = remember(selectedCategory, searchQuery) {
        val query = searchQuery.trim().lowercase()
        VietnameseFinancialInstitutions.filter { inst ->
            val matchCategory = selectedCategory == InstitutionCategory.ALL || inst.category == selectedCategory
            val matchQuery = query.isBlank() ||
                inst.shortName.lowercase().contains(query) ||
                inst.fullName.lowercase().contains(query) ||
                inst.code.lowercase().contains(query) ||
                inst.keywords.any { it.contains(query) }
            matchCategory && matchQuery
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "Mẫu Ngân hàng & Ví điện tử",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "Chọn để tự động điền tên, biểu tượng & màu thẻ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TextButton(
                onClick = { showFullListModal = true },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
            ) {
                Text(
                    text = "Tất cả (${VietnameseFinancialInstitutions.size})",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        // Category Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(InstitutionCategory.entries) { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = {
                        Text(
                            text = cat.label,
                            fontWeight = if (selectedCategory == cat) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp,
                        )
                    },
                )
            }
        }

        // Horizontal List of Institution Presets
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(filteredInstitutions) { inst ->
                val isSelected = selectedInstitution?.id == inst.id
                val instColor = colorFromHex(inst.colorHex)

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isSelected) instColor.copy(alpha = 0.18f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) instColor else tokens.border,
                            shape = RoundedCornerShape(14.dp),
                        )
                        .clickable { onSelectInstitution(inst) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FinancialInstitutionLogo(
                        institution = inst,
                        walletType = inst.type,
                        size = 32.dp,
                    )
                    Column {
                        Text(
                            text = inst.shortName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1,
                        )
                        Text(
                            text = inst.code,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = instColor,
                        )
                    }
                }
            }
        }
    }

    if (showFullListModal) {
        InstitutionCatalogDialog(
            onDismiss = { showFullListModal = false },
            onSelectInstitution = {
                onSelectInstitution(it)
                showFullListModal = false
            },
        )
    }
}

/**
 * Modal xem và tìm kiếm toàn bộ danh mục Ngân hàng & Ví điện tử
 */
@Composable
fun InstitutionCatalogDialog(
    onDismiss: () -> Unit,
    onSelectInstitution: (FinancialInstitution) -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(InstitutionCategory.ALL) }

    val filteredList = remember(searchQuery, selectedCategory) {
        val query = searchQuery.trim().lowercase()
        VietnameseFinancialInstitutions.filter { inst ->
            val matchCat = selectedCategory == InstitutionCategory.ALL || inst.category == selectedCategory
            val matchQuery = query.isBlank() ||
                inst.shortName.lowercase().contains(query) ||
                inst.fullName.lowercase().contains(query) ||
                inst.code.lowercase().contains(query) ||
                inst.keywords.any { it.contains(query) }
            matchCat && matchQuery
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        },
        title = {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Danh sách Ngân hàng & Ví điện tử",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Tìm kiếm và chọn nhanh nguồn tiền",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Search Input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Tìm Vietcombank, Momo, VPBank...") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.AccountBalance,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                )

                // Category Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(InstitutionCategory.entries) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.label, fontSize = 11.sp) },
                        )
                    }
                }

                // Grid list
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    items(filteredList) { inst ->
                        val instColor = colorFromHex(inst.colorHex)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                                .clickable { onSelectInstitution(inst) }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            FinancialInstitutionLogo(
                                institution = inst,
                                walletType = inst.type,
                                size = 38.dp,
                            )
                            Column(
                                modifier = Modifier.weight(1f),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Text(
                                        text = inst.shortName,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                    Text(
                                        text = inst.code,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 10.sp,
                                        color = instColor,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(instColor.copy(alpha = 0.15f))
                                            .padding(horizontal = 4.dp, vertical = 1.dp),
                                    )
                                }
                                Text(
                                    text = inst.fullName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }
        },
    )
}
