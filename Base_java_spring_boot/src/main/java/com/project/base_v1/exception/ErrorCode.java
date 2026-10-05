package com.project.base_v1.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // ============================================================
    // 400xxx - BAD REQUEST
    // ============================================================
    BAD_REQUEST(
            400001,
            HttpStatus.BAD_REQUEST,
            "Yêu cầu không hợp lệ"
    ),

    INVALID_REQUEST_BODY(
            400002,
            HttpStatus.BAD_REQUEST,
            "Dữ liệu gửi lên không hợp lệ"
    ),

    VALIDATION_ERROR(
            400003,
            HttpStatus.BAD_REQUEST,
            "Dữ liệu không đúng định dạng"
    ),

    MISSING_REQUIRED_FIELD(
            400004,
            HttpStatus.BAD_REQUEST,
            "Thiếu thông tin bắt buộc"
    ),

    INVALID_PARAMETER(
            400005,
            HttpStatus.BAD_REQUEST,
            "Tham số không hợp lệ"
    ),

    INVALID_ENUM_VALUE(
            400006,
            HttpStatus.BAD_REQUEST,
            "Giá trị enum không hợp lệ"
    ),

    INVALID_DATE_FORMAT(
            400007,
            HttpStatus.BAD_REQUEST,
            "Định dạng ngày không hợp lệ"
    ),

    INVALID_UUID_FORMAT(
            400008,
            HttpStatus.BAD_REQUEST,
            "Định dạng UUID không hợp lệ"
    ),

    ROOM_NOT_USABLE(
            400020,
            HttpStatus.BAD_REQUEST,
            "Phòng đang bảo trì hoặc ngưng hoạt động, không thể sử dụng"
    ),

    ROOM_CAPACITY_EXCEEDED(
            400021,
            HttpStatus.BAD_REQUEST,
            "Số lượng phòng đã đạt sức chứa tối đa"
    ),

    ROOM_GENDER_MISMATCH(
            400022,
            HttpStatus.BAD_REQUEST,
            "Giới tính không phù hợp với quy định của phòng hoặc tòa nhà"
    ),

    ROOM_OCCUPIED_CANNOT_CHANGE_CAPACITY(
            400023,
            HttpStatus.BAD_REQUEST,
            "Không thể giảm sức chứa nhỏ hơn số người đang ở hiện tại"
    ),

    BED_NOT_AVAILABLE(
            400024,
            HttpStatus.BAD_REQUEST,
            "Giường không ở trạng thái sẵn sàng"
    ),

    BED_CANNOT_BE_ACTIVE_WHEN_ROOM_NOT_USABLE(
            400025,
            HttpStatus.BAD_REQUEST,
            "Không thể kích hoạt giường khi phòng đang bảo trì hoặc ngừng hoạt động"
    ),

    PRICE_POLICY_INVALID_DATES(
            400026,
            HttpStatus.BAD_REQUEST,
            "Ngày kết thúc phải sau ngày bắt đầu"
    ),

    INVALID_REGISTRATION_TRANSITION(400030, HttpStatus.BAD_REQUEST, "Trạng thái hồ sơ đăng ký không cho phép thao tác này"),
    REJECTION_REASON_REQUIRED(400031, HttpStatus.BAD_REQUEST, "Bắt buộc nhập lý do từ chối"),
    INVALID_ASSIGNMENT_DATES(400032, HttpStatus.BAD_REQUEST, "Thời gian phân phòng không hợp lệ"),
    INVALID_CONTRACT_DATES(400033, HttpStatus.BAD_REQUEST, "Thời hạn hợp đồng không hợp lệ"),
    INVALID_CONTRACT_TRANSITION(400034, HttpStatus.BAD_REQUEST, "Trạng thái hợp đồng không cho phép thao tác này"),
    INVALID_REGISTRATION_DATES(400035, HttpStatus.BAD_REQUEST, "Thời gian đăng ký nội trú không hợp lệ"),
    INVALID_BILLING_PERIOD(400040, HttpStatus.BAD_REQUEST, "Kỳ hóa đơn phải là ngày đầu tháng"),
    INVALID_METER_READING(400041, HttpStatus.BAD_REQUEST, "Chỉ số công tơ không hợp lệ"),
    INVOICE_IMMUTABLE(400042, HttpStatus.BAD_REQUEST, "Hóa đơn đã thanh toán hoặc hủy không thể thay đổi"),
    PAYMENT_AMOUNT_INVALID(400043, HttpStatus.BAD_REQUEST, "Số tiền thanh toán không hợp lệ"),
    VNPAY_SIGNATURE_INVALID(400044, HttpStatus.BAD_REQUEST, "Chữ ký VNPay không hợp lệ"),
    VNPAY_NOT_CONFIGURED(400045, HttpStatus.BAD_REQUEST, "VNPay chưa được cấu hình"),
    METER_READING_LOCKED(400046, HttpStatus.BAD_REQUEST, "Chỉ được sửa chỉ số mới nhất và chưa phát hành hóa đơn"),

    // ============================================================
    // 401xxx - UNAUTHORIZED
    // ============================================================
    INVALID_CREDENTIALS(
            401001,
            HttpStatus.UNAUTHORIZED,
            "Tên đăng nhập hoặc mật khẩu không đúng"
    ),

    TOKEN_EXPIRED(
            401002,
            HttpStatus.UNAUTHORIZED,
            "Phiên đăng nhập đã hết hạn"
    ),

    TOKEN_REVOKED(
            401003,
            HttpStatus.UNAUTHORIZED,
            "Token đã bị thu hồi"
    ),

    INVALID_TOKEN(
            401004,
            HttpStatus.UNAUTHORIZED,
            "Token không hợp lệ"
    ),

    UNAUTHENTICATED(
            401005,
            HttpStatus.UNAUTHORIZED,
            "Bạn chưa đăng nhập"
    ),

    REFRESH_TOKEN_EXPIRED(
            401006,
            HttpStatus.UNAUTHORIZED,
            "Refresh token đã hết hạn"
    ),

    REFRESH_TOKEN_INVALID(
            401007,
            HttpStatus.UNAUTHORIZED,
            "Refresh token không hợp lệ"
    ),

    // ============================================================
    // 403xxx - FORBIDDEN
    // ============================================================
    ACCESS_DENIED(
            403001,
            HttpStatus.FORBIDDEN,
            "Bạn không có quyền thực hiện chức năng này"
    ),

    ACCOUNT_DISABLED(
            403002,
            HttpStatus.FORBIDDEN,
            "Tài khoản đã bị khóa"
    ),

    ACCOUNT_NOT_ACTIVE(
            403003,
            HttpStatus.FORBIDDEN,
            "Tài khoản chưa được kích hoạt"
    ),

    REGISTRATION_NOT_ALLOWED(
            403010,
            HttpStatus.FORBIDDEN,
            "Sinh viên không có trong danh sách được phép đăng ký KTX"
    ),

    STUDENT_INACTIVE(
            403011,
            HttpStatus.FORBIDDEN,
            "Hồ sơ sinh viên trong danh sách trường đang ở trạng thái không hoạt động"
    ),

    STUDENT_INFO_MISMATCH(
            400010,
            HttpStatus.BAD_REQUEST,
            "Thông tin đăng ký không khớp với hồ sơ sinh viên trong danh sách của trường"
    ),

    INVALID_CSV_HEADER(
            400011,
            HttpStatus.BAD_REQUEST,
            "Tiêu đề file CSV không đúng định dạng yêu cầu"
    ),

    INVALID_CSV_FILE(
            400012,
            HttpStatus.BAD_REQUEST,
            "File CSV không hợp lệ hoặc không có dữ liệu"
    ),

    STUDENT_ALREADY_REGISTERED(
            409010,
            HttpStatus.CONFLICT,
            "Mã sinh viên này đã được liên kết với một tài khoản người dùng"
    ),

    STUDENT_NOT_FOUND(
            404010,
            HttpStatus.NOT_FOUND,
            "Không tìm thấy hồ sơ sinh viên trong danh sách"
    ),

    // ============================================================
    // 404xxx - NOT FOUND
    // ============================================================
    USER_NOT_FOUND(
            404001,
            HttpStatus.NOT_FOUND,
            "Không tìm thấy người dùng"
    ),

    RESOURCE_NOT_FOUND(
            404002,
            HttpStatus.NOT_FOUND,
            "Không tìm thấy dữ liệu"
    ),

    TOKEN_SESSION_NOT_FOUND(
            404003,
            HttpStatus.NOT_FOUND,
            "Không tìm thấy phiên đăng nhập"
    ),

    API_NOT_FOUND(
            404004,
            HttpStatus.NOT_FOUND,
            "Không tìm thấy API yêu cầu"
    ),

    BUILDING_NOT_FOUND(
            404020,
            HttpStatus.NOT_FOUND,
            "Không tìm thấy tòa nhà"
    ),

    FLOOR_NOT_FOUND(
            404021,
            HttpStatus.NOT_FOUND,
            "Không tìm thấy tầng"
    ),

    ROOM_NOT_FOUND(
            404022,
            HttpStatus.NOT_FOUND,
            "Không tìm thấy phòng"
    ),

    BED_NOT_FOUND(
            404023,
            HttpStatus.NOT_FOUND,
            "Không tìm thấy giường"
    ),

    PRICE_POLICY_NOT_FOUND(
            404024,
            HttpStatus.NOT_FOUND,
            "Không tìm thấy chính sách giá"
    ),

    REGISTRATION_NOT_FOUND(404030, HttpStatus.NOT_FOUND, "Không tìm thấy hồ sơ đăng ký KTX"),
    ASSIGNMENT_NOT_FOUND(404031, HttpStatus.NOT_FOUND, "Không tìm thấy thông tin phân phòng"),
    CONTRACT_NOT_FOUND(404032, HttpStatus.NOT_FOUND, "Không tìm thấy hợp đồng"),
    METER_NOT_FOUND(404040, HttpStatus.NOT_FOUND, "Không tìm thấy công tơ"),
    TARIFF_NOT_FOUND(404041, HttpStatus.NOT_FOUND, "Không tìm thấy đơn giá hiệu lực"),
    INVOICE_NOT_FOUND(404042, HttpStatus.NOT_FOUND, "Không tìm thấy hóa đơn"),
    PAYMENT_NOT_FOUND(404043, HttpStatus.NOT_FOUND, "Không tìm thấy giao dịch thanh toán"),

    // ============================================================
    // 405xxx - METHOD NOT ALLOWED
    // ============================================================
    METHOD_NOT_ALLOWED(
            405001,
            HttpStatus.METHOD_NOT_ALLOWED,
            "Phương thức HTTP không được hỗ trợ"
    ),

    // ============================================================
    // 409xxx - CONFLICT
    // ============================================================
    USERNAME_ALREADY_EXISTS(
            409001,
            HttpStatus.CONFLICT,
            "Tên đăng nhập đã tồn tại"
    ),

    EMAIL_ALREADY_EXISTS(
            409002,
            HttpStatus.CONFLICT,
            "Email đã tồn tại"
    ),

    DATA_ALREADY_EXISTS(
            409003,
            HttpStatus.CONFLICT,
            "Dữ liệu đã tồn tại"
    ),

    DATA_INTEGRITY_VIOLATION(
            409004,
            HttpStatus.CONFLICT,
            "Dữ liệu bị trùng hoặc vi phạm ràng buộc"
    ),

    FOREIGN_KEY_VIOLATION(
            409005,
            HttpStatus.CONFLICT,
            "Dữ liệu liên kết không tồn tại hoặc không hợp lệ"
    ),

    BUILDING_CODE_ALREADY_EXISTS(
            409020,
            HttpStatus.CONFLICT,
            "Mã tòa nhà đã tồn tại"
    ),

    BUILDING_HAS_FLOORS(
            409021,
            HttpStatus.CONFLICT,
            "Không thể xóa tòa nhà đang có tầng"
    ),

    FLOOR_NUMBER_ALREADY_EXISTS(
            409022,
            HttpStatus.CONFLICT,
            "Số tầng đã tồn tại trong tòa nhà này"
    ),

    FLOOR_HAS_ROOMS(
            409023,
            HttpStatus.CONFLICT,
            "Không thể xóa tầng đang có phòng"
    ),

    ROOM_NUMBER_ALREADY_EXISTS(
            409024,
            HttpStatus.CONFLICT,
            "Số phòng đã tồn tại trong tầng này"
    ),

    ROOM_HAS_BEDS(
            409025,
            HttpStatus.CONFLICT,
            "Không thể xóa phòng đang có giường"
    ),

    BED_NUMBER_ALREADY_EXISTS(
            409026,
            HttpStatus.CONFLICT,
            "Số giường đã tồn tại trong phòng này"
    ),

    BED_OCCUPIED(
            409027,
            HttpStatus.CONFLICT,
            "Giường đang có người ở"
    ),

    ACTIVE_REGISTRATION_EXISTS(409030, HttpStatus.CONFLICT, "Bạn đã có hồ sơ đăng ký đang được xử lý"),
    NO_COMPATIBLE_BED(409031, HttpStatus.CONFLICT, "Không còn giường phù hợp với nhu cầu đăng ký"),
    ACTIVE_ASSIGNMENT_EXISTS(409032, HttpStatus.CONFLICT, "Sinh viên hoặc giường đã có phân phòng đang hoạt động"),
    REGISTRATION_ALREADY_ASSIGNED(409033, HttpStatus.CONFLICT, "Hồ sơ đã được phân phòng"),
    ACTIVE_CONTRACT_EXISTS(409034, HttpStatus.CONFLICT, "Sinh viên đã có hợp đồng đang hoạt động"),
    CONTRACT_FOR_ASSIGNMENT_EXISTS(409035, HttpStatus.CONFLICT, "Phân phòng này đã có hợp đồng"),
    ACTIVE_CONTRACT_BLOCKS_ASSIGNMENT_END(409036, HttpStatus.CONFLICT, "Phải thanh lý hợp đồng trước khi kết thúc phân phòng"),
    METER_ALREADY_EXISTS(409040, HttpStatus.CONFLICT, "Phòng đã có công tơ cùng loại đang hoạt động hoặc mã công tơ đã tồn tại"),
    METER_READING_ALREADY_EXISTS(409041, HttpStatus.CONFLICT, "Công tơ đã có chỉ số trong kỳ này"),
    INVOICE_ALREADY_EXISTS(409042, HttpStatus.CONFLICT, "Hợp đồng đã có hóa đơn trong kỳ này"),
    PAYMENT_OVERPAYMENT(409043, HttpStatus.CONFLICT, "Số tiền thanh toán vượt quá công nợ còn lại"),

    // ============================================================
    // 415xxx - UNSUPPORTED MEDIA TYPE
    // ============================================================
    UNSUPPORTED_MEDIA_TYPE(
            415001,
            HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            "Định dạng dữ liệu không được hỗ trợ"
    ),

    // ============================================================
    // 429xxx - TOO MANY REQUESTS
    // ============================================================
    TOO_MANY_REQUESTS(
            429001,
            HttpStatus.TOO_MANY_REQUESTS,
            "Bạn thao tác quá nhiều lần, vui lòng thử lại sau"
    ),

    LOGIN_TOO_MANY_ATTEMPTS(
            429002,
            HttpStatus.TOO_MANY_REQUESTS,
            "Bạn đăng nhập sai quá nhiều lần, vui lòng thử lại sau"
    ),

    // ============================================================
    // 500xxx - INTERNAL SERVER ERROR
    // ============================================================
    SYSTEM_ERROR(
            500001,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Lỗi hệ thống, vui lòng thử lại sau"
    ),

    DATABASE_ERROR(
            500002,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Lỗi cơ sở dữ liệu"
    ),

    TRANSACTION_ERROR(
            500003,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Lỗi xử lý giao dịch"
    ),

    LAZY_LOADING_ERROR(
            500004,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Lỗi tải dữ liệu liên kết"
    ),

    REDIS_ERROR(
            500005,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Lỗi kết nối Redis"
    ),

    CACHE_ERROR(
            500006,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Lỗi xử lý cache"
    ),

    JSON_PROCESSING_ERROR(
            500007,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Lỗi xử lý dữ liệu JSON"
    ),

    WEBSOCKET_ERROR(
            500008,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Lỗi xử lý WebSocket"
    );

    private final int code;
    private final HttpStatus status;
    private final String message;

    ErrorCode(int code, HttpStatus status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }

    public int code() {
        return code;
    }

    public HttpStatus status() {
        return status;
    }

    public String message() {
        return message;
    }
}
