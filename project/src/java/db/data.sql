INSERT INTO Features

    (isChanged, type, seatPitch, seatWidth, newSeatPitch, newSeatWidth,

     videoType, newVideoType, powerType, newPowerType, wifi, newWifi,

     specialFood, newSpecialFood, seatType, newSeatType, price, newPrice)

VALUES

    (FALSE, 0, 30, 18, 30, 18, 'Screen', 'Screen', 'USB', 'USB',

     'Available', 'Available', 'Standard', 'Standard', 'Economy', 'Economy',

     200, 200),

    (FALSE, 1, 38, 21, 38, 21, 'Screen', 'Screen', 'AC', 'AC',

     'Available', 'Available', 'Premium', 'Premium', 'Business', 'Business',

     500, 500),

    (FALSE, 2, 60, 25, 60, 25, 'Screen', 'Screen', 'AC', 'AC',

     'Available', 'Available', 'Premium', 'Premium', 'First Class', 'First Class',

     900, 900);
 
INSERT INTO Customers (name, email)

VALUES

    ('Ashutosh', 'ashutosh@yash.com'),
    
    ('Durgesh', 'durgesh@yash.com'),
    
    ('Customer', 'customer@yash.com');
 
INSERT INTO Cities (cityName)

VALUES

    ('Indore'),

    ('Chennai'),
    
    ('Bengaluru'),
    
    ('Hyderabad'),
    
    ('Ahmedabad'),
    
    ('Pune');
 
INSERT INTO Flights

    (isChanged, flightName, totalSeats, currentSeats, departureCity, arrivalCity,

     departureDate, arrivalDate, oldESeats, oldBSeats, oldFSeats, oldTSeats,

     economySeats, businessSeats, firstSeats)

VALUES

    (FALSE, 'SA-101', 6, 3, 'Pune', 'Indore',

     TIMESTAMP '2027-06-15 09:00:00', TIMESTAMP '2027-06-15 10:15:00',

     0, 0, 0, 0, 3, 3, 0);
 
INSERT INTO Seats (seatNumber, customerEmail, featureType, flightName)

VALUES
	(1, 'customer@yash.com', 'Economy', 'SA-101'),
    (2, 'ashutosh@yash.com', 'Economy', 'SA-101'),
    (3, NULL, 'Economy', 'SA-101'),
    (4, 'durgesh@yash.com', 'Business', 'SA-101'),
    (5, NULL, 'Business', 'SA-101'),
    (6, NULL, 'Business', 'SA-101');
 