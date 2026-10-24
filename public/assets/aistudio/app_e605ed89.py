import os
from datetime import datetime, timedelta
from functools import wraps
from flask import Flask, render_template, request, redirect, url_for, session, flash, jsonify
from werkzeug.security import generate_password_hash, check_password_hash
import mysql.connector
from mysql.connector import Error
from dotenv import load_dotenv

load_dotenv()

app = Flask(__name__)
app.secret_key = os.getenv("FLASK_SECRET_KEY", "dev_secret_key_change_me")
app.permanent_session_lifetime = timedelta(days=14)

DB_CONFIG = {
    'host': os.getenv('DB_HOST', '127.0.0.1'),
    'user': os.getenv('DB_USER', 'root'),
    'password': os.getenv('DB_PASSWORD', ''),
    'database': os.getenv('DB_NAME', 'ministry_of_health_ambulance'),
    'port': int(os.getenv('DB_PORT', '3306')),
}


def get_db_connection():
    return mysql.connector.connect(**DB_CONFIG)


def login_required(view):
    @wraps(view)
    def wrapped(*args, **kwargs):
        if 'user_id' not in session:
            return redirect(url_for('login'))
        return view(*args, **kwargs)
    return wrapped


def admin_required(view):
    @wraps(view)
    def wrapped(*args, **kwargs):
        if 'user_id' not in session or not session.get('is_admin'):
            return redirect(url_for('admin_login'))
        return view(*args, **kwargs)
    return wrapped


@app.route('/')
def index():
    return render_template('index.html')


@app.route('/services')
def services():
    return render_template('services.html')


@app.route('/tips')
def tips():
    return render_template('tips.html')


@app.route('/moh')
def moh():
    return render_template('moh.html')


@app.route('/login', methods=['GET', 'POST'])
def login():
    if request.method == 'POST':
        username = request.form.get('username')
        password = request.form.get('password')
        try:
            conn = get_db_connection()
            cur = conn.cursor(dictionary=True)
            cur.execute("SELECT user_id, username, password_hash, is_admin FROM users WHERE username=%s", (username,))
            user = cur.fetchone()
            cur.close()
            conn.close()
        except Error as e:
            flash(f"Database error: {e}", 'error')
            return render_template('login.html')

        if user and check_password_hash(user['password_hash'], password):
            session.permanent = True
            session['user_id'] = user['user_id']
            session['username'] = user['username']
            session['is_admin'] = bool(user.get('is_admin'))
            flash('Login successful', 'success')
            return redirect(url_for('index'))
        else:
            flash('Invalid credentials', 'error')
    return render_template('login.html')


@app.route('/register', methods=['GET', 'POST'])
def register():
    if request.method == 'POST':
        username = request.form.get('username')
        email = request.form.get('email')
        password = request.form.get('password')
        full_name = request.form.get('full_name')
        phone_number = request.form.get('phone_number')
        password_hash = generate_password_hash(password)
        try:
            conn = get_db_connection()
            cur = conn.cursor()
            cur.execute(
                "INSERT INTO users (username, password_hash, email, full_name, phone_number) VALUES (%s, %s, %s, %s, %s)",
                (username, password_hash, email, full_name, phone_number)
            )
            conn.commit()
            cur.close()
            conn.close()
            flash('Registration successful. Please login.', 'success')
            return redirect(url_for('login'))
        except Error as e:
            flash(f"Registration failed: {e}", 'error')
    return render_template('register.html')


@app.route('/logout')
def logout():
    session.clear()
    flash('Logged out successfully', 'success')
    return redirect(url_for('index'))


@app.route('/book', methods=['GET', 'POST'])
@login_required
def book():
    if request.method == 'POST':
        transport_type = request.form.get('transport_type')
        pickup_location = request.form.get('pickup_location')
        destination = request.form.get('destination')
        medical_problem = request.form.get('medical_problem')
        emergency_type = request.form.get('emergency_type')
        patient_history = request.form.get('patient_history')
        booking_time = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
        try:
            conn = get_db_connection()
            cur = conn.cursor()
            cur.execute(
                "INSERT INTO bookings (user_id, requested_transport, pickup_location, destination, medical_problem, emergency_type, patient_history, booking_time, current_status) VALUES (%s,%s,%s,%s,%s,%s,%s,%s,%s)",
                (session['user_id'], transport_type, pickup_location, destination, medical_problem, emergency_type, patient_history, booking_time, 'Pending')
            )
            conn.commit()
            booking_id = cur.lastrowid
            cur.close()
            conn.close()
            flash(f'Booking created. Your Booking ID is {booking_id}.', 'success')
            return redirect(url_for('track'))
        except Error as e:
            flash(f"Booking failed: {e}", 'error')
    return render_template('book.html')


@app.route('/track', methods=['GET', 'POST'])
def track():
    booking = None
    if request.method == 'POST':
        booking_id = request.form.get('booking_id')
        try:
            conn = get_db_connection()
            cur = conn.cursor(dictionary=True)
            cur.execute(
                """
                SELECT b.*, a.current_location, a.status AS ambulance_status, a.transport_type
                FROM bookings b
                LEFT JOIN ambulances a ON b.ambulance_id = a.ambulance_id
                WHERE b.booking_id = %s
                """,
                (booking_id,)
            )
            booking = cur.fetchone()
            cur.close()
            conn.close()
        except Error as e:
            flash(f"Lookup failed: {e}", 'error')
    return render_template('track.html', booking=booking)


@app.route('/api/track/<int:booking_id>')
def api_track(booking_id):
    try:
        conn = get_db_connection()
        cur = conn.cursor(dictionary=True)
        cur.execute(
            """
            SELECT b.booking_id, b.current_status, b.estimated_arrival, a.current_location, a.status AS ambulance_status
            FROM bookings b
            LEFT JOIN ambulances a ON b.ambulance_id = a.ambulance_id
            WHERE b.booking_id = %s
            """,
            (booking_id,)
        )
        row = cur.fetchone()
        cur.close()
        conn.close()
        if not row:
            return jsonify({'error': 'Not found'}), 404
        return jsonify({
            'booking_id': row['booking_id'],
            'status': row['current_status'],
            'eta': row['estimated_arrival'].strftime('%Y-%m-%d %H:%M:%S') if row['estimated_arrival'] else None,
            'location': row.get('current_location')
        })
    except Error as e:
        return jsonify({'error': str(e)}), 500


# Admin routes
@app.route('/admin/login', methods=['GET', 'POST'])
def admin_login():
    # If already logged in as admin, go straight to dashboard
    if session.get('is_admin'):
        return redirect(url_for('admin_dashboard'))
    if request.method == 'POST':
        username = request.form.get('username')
        password = request.form.get('password')
        try:
            conn = get_db_connection()
            cur = conn.cursor(dictionary=True)
            cur.execute("SELECT user_id, username, password_hash, is_admin FROM users WHERE username=%s AND is_admin=TRUE", (username,))
            user = cur.fetchone()
            cur.close()
            conn.close()
        except Error as e:
            flash(f"Database error: {e}", 'error')
            return render_template('admin_login.html')

        if user and check_password_hash(user['password_hash'], password):
            session.permanent = True
            session['user_id'] = user['user_id']
            session['username'] = user['username']
            session['is_admin'] = True
            flash('Admin login successful', 'success')
            return redirect(url_for('admin_dashboard'))
        else:
            flash('Invalid admin credentials', 'error')
    return render_template('admin_login.html')


@app.route('/admin')
@admin_required
def admin_dashboard():
    stats = {'pending': 0, 'dispatched': 0, 'available_ambulances': 0}
    try:
        conn = get_db_connection()
        cur = conn.cursor()
        cur.execute("SELECT COUNT(*) FROM bookings WHERE current_status='Pending'")
        stats['pending'] = cur.fetchone()[0]
        cur.execute("SELECT COUNT(*) FROM bookings WHERE current_status IN ('Dispatched','Confirmed')")
        stats['dispatched'] = cur.fetchone()[0]
        cur.execute("SELECT COUNT(*) FROM ambulances WHERE status='Available'")
        stats['available_ambulances'] = cur.fetchone()[0]
        cur.close()
        conn.close()
    except Error:
        pass
    return render_template('admin_dashboard.html', stats=stats)


@app.route('/admin/bookings/pending')
@admin_required
def admin_pending_bookings():
    rows = []
    try:
        conn = get_db_connection()
        cur = conn.cursor(dictionary=True)
        cur.execute("SELECT * FROM bookings WHERE current_status='Pending' ORDER BY booking_time DESC")
        rows = cur.fetchall()
        cur.close()
        conn.close()
    except Error as e:
        flash(str(e), 'error')
    return render_template('admin_bookings.html', bookings=rows)


@app.route('/admin/assign/<int:booking_id>', methods=['GET', 'POST'])
@admin_required
def admin_assign_ambulance(booking_id):
    try:
        conn = get_db_connection()
        cur = conn.cursor(dictionary=True)
        # Get booking
        cur.execute("SELECT * FROM bookings WHERE booking_id=%s", (booking_id,))
        booking = cur.fetchone()
        if not booking:
            flash('Booking not found', 'error')
            return redirect(url_for('admin_pending_bookings'))
        # List available ambulances
        cur.execute("SELECT * FROM ambulances WHERE status='Available' AND transport_type=%s", (booking['requested_transport'],))
        ambulances = cur.fetchall()
        if request.method == 'POST':
            ambulance_id = request.form.get('ambulance_id')
            # Assign
            cur2 = conn.cursor()
            cur2.execute("UPDATE bookings SET ambulance_id=%s, current_status=%s WHERE booking_id=%s", (ambulance_id, 'Dispatched', booking_id))
            cur2.execute("UPDATE ambulances SET status='Dispatched', last_updated=%s WHERE ambulance_id=%s", (datetime.now().strftime('%Y-%m-%d %H:%M:%S'), ambulance_id))
            conn.commit()
            cur2.close()
            flash('Ambulance assigned and dispatched.', 'success')
            cur.close(); conn.close()
            return redirect(url_for('admin_pending_bookings'))
        cur.close(); conn.close()
        return render_template('admin_assign.html', booking=booking, ambulances=ambulances)
    except Error as e:
        flash(str(e), 'error')
        return redirect(url_for('admin_pending_bookings'))


@app.route('/admin/ambulances', methods=['GET', 'POST'])
@admin_required
def admin_ambulances():
    message = None
    try:
        conn = get_db_connection()
        if request.method == 'POST':
            vehicle_number = request.form.get('vehicle_number')
            transport_type = request.form.get('transport_type')
            current_location = request.form.get('current_location')
            status = request.form.get('status', 'Available')
            cur = conn.cursor()
            cur.execute(
                "INSERT INTO ambulances (vehicle_number, transport_type, status, current_location, last_updated) VALUES (%s,%s,%s,%s,%s)",
                (vehicle_number, transport_type, status, current_location, datetime.now().strftime('%Y-%m-%d %H:%M:%S'))
            )
            conn.commit()
            cur.close()
            message = 'Ambulance added.'
        cur = conn.cursor(dictionary=True)
        cur.execute("SELECT * FROM ambulances ORDER BY ambulance_id DESC")
        rows = cur.fetchall()
        cur.close(); conn.close()
        return render_template('admin_ambulances.html', ambulances=rows, message=message)
    except Error as e:
        flash(str(e), 'error')
        return render_template('admin_ambulances.html', ambulances=[], message=None)


@app.route('/admin/ambulances/update/<int:ambulance_id>', methods=['POST'])
@admin_required
def admin_update_ambulance(ambulance_id):
    status = request.form.get('status')
    current_location = request.form.get('current_location')
    try:
        conn = get_db_connection()
        cur = conn.cursor()
        cur.execute(
            "UPDATE ambulances SET status=%s, current_location=%s, last_updated=%s WHERE ambulance_id=%s",
            (status, current_location, datetime.now().strftime('%Y-%m-%d %H:%M:%S'), ambulance_id)
        )
        conn.commit()
        cur.close(); conn.close()
        flash('Ambulance updated.', 'success')
    except Error as e:
        flash(str(e), 'error')
    return redirect(url_for('admin_ambulances'))


@app.route('/admin/bookings/active')
@admin_required
def admin_active_bookings():
    rows = []
    try:
        conn = get_db_connection()
        cur = conn.cursor(dictionary=True)
        cur.execute("SELECT * FROM bookings WHERE current_status IN ('Dispatched','Confirmed','In Transit') ORDER BY booking_time DESC")
        rows = cur.fetchall()
        cur.close(); conn.close()
    except Error as e:
        flash(str(e), 'error')
    return render_template('admin_active_bookings.html', bookings=rows)


@app.route('/admin/bookings/update/<int:booking_id>', methods=['POST'])
@admin_required
def admin_update_booking(booking_id):
    new_status = request.form.get('status')
    eta = request.form.get('estimated_arrival')  
    try:
        conn = get_db_connection()
        cur = conn.cursor()
        if eta:
            cur.execute("UPDATE bookings SET current_status=%s, estimated_arrival=%s WHERE booking_id=%s", (new_status, eta, booking_id))
        else:
            cur.execute("UPDATE bookings SET current_status=%s WHERE booking_id=%s", (new_status, booking_id))
        conn.commit()
        cur.close(); conn.close()
        flash('Booking updated.', 'success')
    except Error as e:
        flash(str(e), 'error')
    return redirect(url_for('admin_active_bookings'))


@app.route('/admin/users')
@admin_required
def admin_users():
    q = request.args.get('q', '').strip()
    rows = []
    try:
        conn = get_db_connection()
        cur = conn.cursor(dictionary=True)
        if q:
            cur.execute("SELECT user_id, username, email, full_name, phone_number, is_admin FROM users WHERE username LIKE %s OR email LIKE %s ORDER BY user_id DESC", (f"%{q}%", f"%{q}%"))
        else:
            cur.execute("SELECT user_id, username, email, full_name, phone_number, is_admin FROM users ORDER BY user_id DESC")
        rows = cur.fetchall()
        cur.close(); conn.close()
    except Error as e:
        flash(str(e), 'error')
    return render_template('users.html', users=rows, q=q)


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=int(os.getenv('PORT', '5000')), debug=True)

                                                    
