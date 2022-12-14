package com.stelvak.lil.learningspring.business.services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.stelvak.lil.learningspring.business.DTOs.GuestDTO;
import com.stelvak.lil.learningspring.business.DTOs.RoomDTO;
import com.stelvak.lil.learningspring.business.DTOs.RoomReservationDTO;
import com.stelvak.lil.learningspring.data.Guest;
import com.stelvak.lil.learningspring.data.GuestRepository;
import com.stelvak.lil.learningspring.data.Reservation;
import com.stelvak.lil.learningspring.data.ReservationRepository;
import com.stelvak.lil.learningspring.data.Room;
import com.stelvak.lil.learningspring.data.RoomRepository;
import com.stelvak.lil.learningspring.util.DTOConvertUtils;

@Service
public class ReservationService {
    private final RoomRepository roomRepository;
    private final GuestRepository guestRepository;
    private final ReservationRepository reservationRepository;
    private final DTOConvertUtils dtoConvertUtils;

    public ReservationService(RoomRepository roomRepository, GuestRepository guestRepository,
            ReservationRepository reservationRepository, DTOConvertUtils dtoConvertUtils) {
        this.roomRepository = roomRepository;
        this.guestRepository = guestRepository;
        this.reservationRepository = reservationRepository;
        this.dtoConvertUtils = dtoConvertUtils;
    }

    public Guest insertGuest(GuestDTO guestDTO) {
        return guestRepository.save(
            dtoConvertUtils.convertGuestDTOtoGuest(guestDTO)
        );
    }

    public List<GuestDTO> getAllGuestInfo() {
        return dtoConvertUtils.convertAllGuestToGuestDTO(
            this.guestRepository.findAll()
        );
    }

    public List<RoomDTO> getAllRoomsInfo() {
        return dtoConvertUtils.convertAllRoomToRoomDTO(roomRepository.findAll());
    }

    public List<RoomReservationDTO> getRoomReservationsForDate(Date date) {
        Iterable<Room> rooms = this.roomRepository.findAll();
        Map<Long, RoomReservationDTO> roomReservationMap = new HashMap<>();
        rooms.forEach(room -> {
            RoomReservationDTO roomReservation = new RoomReservationDTO();
            roomReservation.setRoomId(room.getId());
            roomReservation.setRoomName(room.getName());
            roomReservation.setRoomNumber(room.getRoomNumber());
            roomReservationMap.put(room.getId(), roomReservation);
        });
        Iterable<Reservation> reservations = this.reservationRepository.findReservationByReservationDate(new java.sql.Date(date.getTime()));
        reservations.forEach(reservation -> {
            RoomReservationDTO roomReservation = roomReservationMap.get(reservation.getRoomId());
            roomReservation.setDate(date);
            Guest guest = this.guestRepository.findById(reservation.getGuestId()).get();
            roomReservation.setFirstName(guest.getFirstName());
            roomReservation.setLastName(guest.getLastName());
            roomReservation.setGuestId(guest.getGuestId());
        });
        List<RoomReservationDTO> roomReservations = new ArrayList<>();
        for (Long id : roomReservationMap.keySet()) {
            roomReservations.add(roomReservationMap.get(id));
        }
        roomReservations.sort(new Comparator<RoomReservationDTO>() {
            @Override
            public int compare(RoomReservationDTO o1, RoomReservationDTO o2) {
                if (o1.getRoomName().equals(o2.getRoomName())) {
                    return o1.getRoomNumber().compareTo(o2.getRoomNumber());
                }
                return o1.getRoomName().compareTo(o2.getRoomName());
            }
        });
        return roomReservations;
    }
}
